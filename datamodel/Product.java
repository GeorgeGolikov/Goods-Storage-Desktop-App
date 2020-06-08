package sample.datamodel;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class Product
{
    private int id_;
    private int prodid_;
    private String title_;
    private double price_;

    public Product(int id, int prodid, String title, double price)
    {
        this.id_ = id;
        this.prodid_ = prodid;
        this.title_ = title;
        this.price_ = price;
    }
    public Product(ResultSet rs) throws SQLException
    {
        this.id_ = rs.getInt("id");
        this.prodid_ = rs.getInt("prodid");
        this.title_ = rs.getString("title");
        this.price_ = rs.getDouble("cost");
    }

    public void setId(int id)
    {
        this.id_ = id;
    }
    public int getId()
    {
        return id_;
    }

    public void setProdid(int prodid)
    {
        this.prodid_ = prodid;
    }
    public int getProdid()
    {
        return prodid_;
    }

    public void setTitle(String title)
    {
        this.title_ = title;
    }
    public String getTitle()
    {
        return title_;
    }

    public void setPrice(double price)
    {
        this.price_ = price;
    }
    public double getPrice()
    {
        return price_;
    }
}
