package com.lin.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lin.gulimall.product.service.CategoryBrandRelationService;
import com.lin.gulimall.product.vo.Catelog2Vo;
import org.omg.CORBA.TIMEOUT;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.Query;

import com.lin.gulimall.product.dao.CategoryDao;
import com.lin.gulimall.product.entity.CategoryEntity;
import com.lin.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(new Query<CategoryEntity>().getPage(params), new QueryWrapper<CategoryEntity>());

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1.查询出所有的分类数据
        List<CategoryEntity> entities = baseMapper.selectList(null);

        // 2.将查询的数据组装成父子的树形结构
        // 2.1).找到所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity -> categoryEntity.getParentCid() == 0).map(categoryEntity -> {
            categoryEntity.setChildren(getChildren(categoryEntity, entities));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return level1Menus;
    }

    @Override
    public void removeMenusByIds(List<Long> catIds) {
        // TODO 1.首先需要检查当前删除的菜单，是否被别的地方引用

        baseMapper.deleteBatchIds(catIds);
    }

    @Override
    public Long[] findCateLogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> intactPath = findParentPath(catelogId, paths);
        Collections.reverse(intactPath);
        return intactPath.toArray(new Long[0]);
    }

    /**
     * 级联更新所有关联的数据
     *
     * @param category 菜单实体类
     */

    /*@Caching(evict = {
            @CacheEvict(value = "category", key = "'getLevel1Categorys'"),
            @CacheEvict(value = "category", key = "'getCatalogJson'")
    })*/
    @CacheEvict(value = "category", allEntries = true)
    @Transactional
    @Override
    public void updateDetail(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getCatId())) {
            categoryBrandRelationService.updateDetail(category.getCatId(), category.getName());
        }
    }

    @Cacheable(value = {"category"}, key = "#root.method.name",sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return baseMapper.selectList(
                new QueryWrapper<CategoryEntity>().eq("parent_cid", 0)
        );
    }

    @Cacheable(value = "category", key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        System.out.println("查询数据库...");
        // 将数据库的多次查询变为一次
        List<CategoryEntity> selectList = baseMapper.selectList(new QueryWrapper<CategoryEntity>(null));
        // 查出所有的一级分类
        List<CategoryEntity> level1Categorys = getPrent_cid(selectList, 0L);
        // 封装数据
        Map<String, List<Catelog2Vo>> parentCid = level1Categorys.stream()
                .collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                    // 每一个的一级分类，查到这个一级分类的二级分类
                    List<CategoryEntity> level2Categorys = getPrent_cid(selectList, v.getCatId());
                    // 将上面的结果封装成指定的格式
                    List<Catelog2Vo> catelog2VoList = null;
                    if (level2Categorys != null) {
                        catelog2VoList = level2Categorys.stream().map(item -> {
                            Catelog2Vo catelog2Vo = new Catelog2Vo(
                                    v.getCatId().toString(), null, item.getCatId().toString(), item.getName()
                            );
                            // 找到当前二级分类的三级分类封装成VO
                            List<CategoryEntity> level3Categorys = getPrent_cid(selectList, item.getCatId());
                            if (level3Categorys != null) {
                                List<Catelog2Vo.Catelog3Vo> catelog3VoList = level3Categorys.stream().map(l3 -> {
                                    return new Catelog2Vo.Catelog3Vo(
                                            item.getCatId().toString(), l3.getCatId().toString(), l3.getName()
                                    );
                                }).collect(Collectors.toList());
                                catelog2Vo.setCatalog3List(catelog3VoList);
                            }
                            return catelog2Vo;
                        }).collect(Collectors.toList());
                    }
                    return catelog2VoList;
                }));
        return parentCid;
    }

    // TODO:产生堆外内存溢出：
    public Map<String, List<Catelog2Vo>> getCatalogJson2() {
        /*
            1、空结果缓存：解决缓存穿透问题
            2、设计过期时间（加随机值）：解决缓存雪崩问题
            3、加锁：解决缓存击穿问题
         */

        // 1、加入缓存逻辑，缓存中存的数据是json字符串(JSON是跨语言，跨平台兼容的)
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            System.out.println("缓存不命中，将要查询数据库...");
            // 2、缓存中没有这些数据，从数据库当中查询
            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDb();

            return catalogJsonFromDb;
        }

        // 4、拿出的json字符串，还要逆转为能用的对象类型【序列化和反序列化】
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(
                catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
                });

        return result;
    }

    /*
        缓存里面的数据如何和数据库保持一致性
        1）双写模式
        2）失效模式
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {
        // 1、一定要注意锁的名字；锁的粒度，粒度越细越快
        // 锁的粒度：具体缓存的是某个数据，11号商品：product-11-lock
        System.out.println("获取分布式锁成功...");
        RLock lock = redisson.getLock("catalogJson-lock");
        lock.lock();

        Map<String, List<Catelog2Vo>> dataFromDB;
        try {
            dataFromDB = getCatalogJsonFromDb();
        } finally {
            lock.unlock();
        }
        return dataFromDB;
    }

    // 从数据库查询并封装分类数据
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDb() {
        // 将数据库的多次查询变为一次
        List<CategoryEntity> selectList = baseMapper.selectList(new QueryWrapper<CategoryEntity>(null));
        // 查出所有的一级分类
        List<CategoryEntity> level1Categorys = getPrent_cid(selectList, 0L);
        // 封装数据
        Map<String, List<Catelog2Vo>> parentCid = level1Categorys.stream()
                .collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                    // 每一个的一级分类，查到这个一级分类的二级分类
                    List<CategoryEntity> level2Categorys = getPrent_cid(selectList, v.getCatId());
                    // 将上面的结果封装成指定的格式
                    List<Catelog2Vo> catelog2VoList = null;
                    if (level2Categorys != null) {
                        catelog2VoList = level2Categorys.stream().map(item -> {
                            Catelog2Vo catelog2Vo = new Catelog2Vo(
                                    v.getCatId().toString(), null, item.getCatId().toString(), item.getName()
                            );
                            // 找到当前二级分类的三级分类封装成VO
                            List<CategoryEntity> level3Categorys = getPrent_cid(selectList, item.getCatId());
                            if (level3Categorys != null) {
                                List<Catelog2Vo.Catelog3Vo> catelog3VoList = level3Categorys.stream().map(l3 -> {
                                    return new Catelog2Vo.Catelog3Vo(
                                            item.getCatId().toString(), l3.getCatId().toString(), l3.getName()
                                    );
                                }).collect(Collectors.toList());
                                catelog2Vo.setCatalog3List(catelog3VoList);
                            }
                            return catelog2Vo;
                        }).collect(Collectors.toList());
                    }
                    return catelog2VoList;
                }));
        // 3、查到的数据再放入缓存中，将对象转为json字符串
        String jsonString = JSON.toJSONString(parentCid);
        redisTemplate.opsForValue().set("catalogJSON", jsonString, 1, TimeUnit.DAYS);

        return parentCid;
    }

    private List<CategoryEntity> getPrent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
        return collect;
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity category = this.getById(catelogId);
        if (category.getParentCid() != 0) {
            findParentPath(category.getParentCid(), paths);
        }
        return paths;
    }

    // 递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            categoryEntity.setChildren(getChildren(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
    }

}