package com.fruit.dao;

import com.fruit.pojo.Fruit;

import java.util.List;

public interface FruitDAO {
    //获取指定的页码上的所有的库存列表信息
    List<Fruit> getFruitList(Integer pageNo,String keyword);

    Fruit getFruitByFid(Integer fid);

    void updateFruit(Fruit fruit);

    void delFruit(Integer fid);

    void addFruit(Fruit fruit);

    int getFruitCount(String keyword);
}
