package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import sample.datamodel.Product;
import sample.datamodel.jdbc.DataAccess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Controller
{
    private static DataAccess dataObject_;

    /* Поля ввода данных */
    @FXML
    private TextField prodidForAdd_;

    @FXML
    private TextField titleForAdd_;

    @FXML
    private TextField priceForAdd_;

    @FXML
    private TextField titleForPrice_;

    @FXML
    private TextField priceStartForFilter_;

    @FXML
    private TextField priceEndForFilter_;

    /* Кнопка Add */
    @FXML
    private void addProduct()
    {
        try (Scanner scan1 = new Scanner(prodidForAdd_.getText()))
        {
            if (scan1.hasNextInt())
            {
                try (Scanner scan2 = new Scanner(titleForAdd_.getText()))
                {
                    if (scan2.hasNext())
                    {
                        String title = scan2.next();
                        if (title.matches("(?i).*[a-zа-я].*"))
                        {
                            try (Scanner scan3 = new Scanner(priceForAdd_.getText()))
                            {
                                if (scan3.hasNextDouble())
                                {
                                    if (dataObject_ != null)
                                    {
                                        int prodid = scan1.nextInt();
                                        double price = scan3.nextDouble();
                                        System.out.println(prodid);

                                        try
                                        {
                                            dataObject_.addItem(tableName_, prodid, title, price);
                                            products_.add(new Product(dataObject_.getItemID(tableName_, title),
                                                                      prodid, title, price));
                                            prodidForAdd_.clear();
                                            titleForAdd_.clear();
                                            priceForAdd_.clear();
                                        }
                                        catch (SQLException e)
                                        {
                                            if (e.getErrorCode() == 1062)
                                            {
                                                String warning = "Товар с таким именем / id уже существует!";
                                                pushWarning(warning);
                                            }
                                            else
                                            {
                                                String warning = "Проблемы соединения с базой!";
                                                pushWarning(warning);
                                            }
                                        }
                                    }
                                    else
                                    {
                                        String warning = "Проблемы соединения с базой! Она закрыта.";
                                        pushWarning(warning);
                                    }
                                }
                                else
                                {
                                    String warning = "Поле price заполнено неверно!";
                                    pushWarning(warning);
                                }
                            }
                        }
                        else
                        {
                            String warning = "Поле title заполнено неверно!";
                            pushWarning(warning);
                        }
                    }
                    else
                    {
                        String warning = "Поле title пусто!";
                        pushWarning(warning);
                    }
                }
            }
            else
            {
                String warning = "Поле prodid заполнено неверно!";
                pushWarning(warning);
            }
        }

    }

    /* Кнопка Delete */
    @FXML
    private Button deleteButton_;

    @FXML
    private void deleteProduct()
    {
        ObservableList<Product> selectedProducts = tableProducts_.getSelectionModel().getSelectedItems();
        // здесь модель выборки подразумевает выбор одновременно только одного продукта
        if (selectedProducts.isEmpty())
        {
            String warning = "Продукт не выбран!";
            pushWarning(warning);
            return;
        }
        String title = selectedProducts.get(0).getTitle();
        try
        {
            dataObject_.deleteItem(tableName_, title);
            selectedProducts.forEach(products_::remove);
        }
        catch (SQLException e)
        {
            String warning = "Не удалось удалить товар из базы!";
            pushWarning(warning);
        }
        catch (RuntimeException e)
        {

        }
        finally
        {
            deleteButton_.setDisable(true);
        }
    }

    /* изменение цены товара */
    private void editPrice(Product prod, Double newPrice)
    {
        try
        {
            dataObject_.changeItemPrice(tableName_, prod.getTitle(), newPrice);
            prod.setPrice(newPrice);
        }
        catch (SQLException e)
        {
            String warning = "Не удалось изменить цену товара в базе!";
            pushWarning(warning);
        }
    }

    /* кнопка Price */
    @FXML
    private void getItemPrice()
    {
        String title = titleForPrice_.getText();
        if (!title.equals(""))
        {
            FilteredList<Product> filteredProducts_;
            filteredProducts_ = new FilteredList<Product>(products_, p -> p.getTitle().contains(title));
            tableProducts_.setItems(filteredProducts_);
        }
        else tableProducts_.setItems(products_);
    }

    /* кнопка Filter */
    @FXML
    private void filter()
    {
        try (Scanner scan1 = new Scanner(priceStartForFilter_.getText()))
        {
            if (scan1.hasNextDouble())
            {
                try (Scanner scan2 = new Scanner(priceEndForFilter_.getText()))
                {
                    if (scan2.hasNextDouble())
                    {
                        double priceStart = scan1.nextDouble();
                        double priceEnd = scan2.nextDouble();
                        FilteredList<Product> filteredProducts_;
                        filteredProducts_ = new FilteredList<Product>(products_, p ->
                                                                                 p.getPrice() >= priceStart &&
                                                                                 p.getPrice() <= priceEnd);
                        tableProducts_.setItems(filteredProducts_);
                    }
                    else
                    {
                        String warning = "Поле priceEnd заполнено неверно!";
                        pushWarning(warning);
                    }
                }
            }
            else
            {
                if (priceStartForFilter_.getText().equals("") && priceEndForFilter_.getText().equals(""))
                {
                    tableProducts_.setItems(products_);
                    return;
                }

                String warning = "Поле priceStart заполнено неверно!";
                pushWarning(warning);
            }
        }
    }

    /* создание окна предупреждения */
    public static void pushWarning(String warningText)
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(Controller.class.getResource("warning.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Warning");
            Scene scene = new Scene(root, 330, 200);
            stage.setScene(scene);
            WarningController controller = loader.getController();
            controller.setLabelText(warningText);
            stage.show();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.out.println("Problems loading the warning");
        }
    }

    /* отображение таблицы */
    private ObservableList<Product> products_ = FXCollections.observableArrayList();

    @FXML
    private TableView<Product> tableProducts_;

    @FXML
    private TableColumn<Product, Integer> idColumn_;

    @FXML
    private TableColumn<Product, Integer> prodidColumn_;

    @FXML
    private TableColumn<Product, String> titleColumn_;

    @FXML
    private TableColumn<Product, Double> priceColumn_;

    @FXML
    private GridPane functionalArea_;

    @FXML
    private void initialize() throws SQLException
    {
        initData();

        idColumn_.setCellValueFactory(new PropertyValueFactory<Product, Integer>("id"));
        prodidColumn_.setCellValueFactory(new PropertyValueFactory<Product, Integer>("prodid"));
        titleColumn_.setCellValueFactory(new PropertyValueFactory<Product, String>("title"));
        priceColumn_.setCellValueFactory(new PropertyValueFactory<Product, Double>("price"));
        priceColumn_.setCellFactory(TextFieldTableCell.<Product, Double>forTableColumn(new DoubleStringConverter()));
        priceColumn_.setOnEditCommit(e -> {
            Double newPrice = e.getNewValue();
            editPrice(e.getTableView().getItems().get(e.getTablePosition().getRow()), newPrice);
        });

        tableProducts_.setItems(products_);
        tableProducts_.setOnMouseClicked(e -> {
            if (e.getY() > 25) deleteButton_.setDisable(false);});
        functionalArea_.setCursor(Cursor.CLOSED_HAND);
    }

    private void initData() throws SQLException
    {
        try (ResultSet rs = dataObject_.showAllGui(tableName_))
        {
            while (rs.next())
            {
                products_.add(new Product(rs));
            }
        }
    }

    /* изначальное НЕГРАФИЧЕСКОЕ заполнение таблицы */
    private static String tableName_ = "goods";

    public static void initTable() throws SQLException
    {
        final int goodAmount = 10;

        dataObject_ = DataAccess.getDataAccessObject();
        dataObject_.createTable(tableName_,
                new String[]{"prodid", "INT", "title", "VARCHAR(50)", "cost", "DOUBLE"});
        dataObject_.fillTable(tableName_, goodAmount);
    }
}
