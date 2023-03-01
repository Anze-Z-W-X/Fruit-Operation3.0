package com.fruit.dao.impl;

import com.fruit.dao.FruitDAO;
import com.fruit.dao.impl.base.BaseDAO;
import com.fruit.pojo.Fruit;

import java.util.List;

public class FruitDAOImpl extends BaseDAO<Fruit> implements FruitDAO {
    @Override
    public List<Fruit> getFruitList(Integer pageNo,String keyword) {
        return super.executeQuery("select * from t_fruit where fname like ? or remark like ? limit ? , 5" , "%"+keyword+"%","%"+keyword+"%",(pageNo-1)*5);
    }

    @Override
    public Fruit getFruitByFid(Integer fid) {
        return super.load("select * from t_fruit where fid = ? " , fid);
    }

    @Override
    public void updateFruit(Fruit fruit) {
        String sql = "update t_fruit set fname = ? , price = ? , fcount = ? , remark = ? where fid = ? " ;
        super.executeUpdate(sql,fruit.getFname(),fruit.getPrice(),fruit.getFcount(),fruit.getRemark(),fruit.getFid());
    }

    @Override
    public void delFruit(Integer fid) {
        super.executeUpdate("delete from t_fruit where fid = ? " , fid) ;
    }

    @Override
    public void addFruit(Fruit fruit) {
        String sql = "insert into t_fruit values(0,?,?,?,?)";
        super.executeUpdate(sql,fruit.getFname(),fruit.getPrice(),fruit.getFcount(),fruit.getRemark());
    }

    @Override
    public int getFruitCount(String keyword) {
        return ((Long)super.executeComplexQuery("select count(*) from t_fruit where fname like ? or remark like ?","%"+keyword+"%","%"+keyword+"%")[0]).intValue();
    }
}

// 39行报错  ClassCastException: java.lang.Long cannot be cast to java.lang.Integer