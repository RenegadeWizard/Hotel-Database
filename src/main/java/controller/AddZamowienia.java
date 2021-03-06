package main.java.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import main.java.Main;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;

import java.sql.*;
import java.util.ArrayList;

public class AddZamowienia {
    private Controller controller;
    private Zamowienia zamowienia;
    private boolean look, edit;
    private String id;
    @FXML
    private ComboBox<String> epracownicy;
    @FXML
    private ComboBox<String> edania;
    @FXML
    private DatePicker zdata;
    @FXML
    private VBox daniaScroll;
    @FXML
    private Button saveButton, editButton, delButton;

    private ArrayList<String> dania = new ArrayList<>();


    public AddZamowienia(Controller controller, Zamowienia zamowienia){
        this.controller = controller;
        this.zamowienia = zamowienia;
        this.look = false;
        this.edit = true;
    }

    public AddZamowienia(Controller controller, Zamowienia zamowienia, String id){
        this.controller = controller;
        this.zamowienia = zamowienia;
        this.id = id;
        this.look = true;
        this.edit = false;
    }
    @FXML
    private void initialize() {
        if (look) {
            try {
                String str = "SELECT z.*, p.nazwisko FROM hotel_zamowienia z inner join hotel_pracownicy p on(p.pesel=z.pracownik) WHERE id_zamowienia=" + id;
                PreparedStatement stmt = zamowienia.dataBase.getCon().prepareStatement(str);
                ResultSet rs = stmt.executeQuery();
                rs.next();
                epracownicy.setValue(rs.getString("nazwisko"));
                epracownicy.setDisable(true);
                zdata.setValue(rs.getDate("data_zamowienia").toLocalDate());
                zdata.setDisable(true);
                edania.setDisable(true);
                getDania();
                for(String i : dania)
                    daniaScroll.getChildren().add(createDanieButton(i));
                saveButton.setVisible(false);
                rs.close();
                stmt.close();
            }catch (Exception ex){
//                ex.printStackTrace();
            }
        } else {
            editButton.setVisible(false);
            delButton.setVisible(false);
        }
        ObservableList<String> observPracownicy = FXCollections.observableArrayList();
        observPracownicy.addAll(zamowienia.dataBase.getSomePracownicy());
        epracownicy.setItems(observPracownicy);
        ObservableList<String> observDania = FXCollections.observableArrayList();
        observDania.addAll(zamowienia.dataBase.getSomeDania());
        edania.setItems(observDania);
    }
    @FXML
    private void addZamowienie(){
        if(look && checkCorrectness()){
            edit = false;
            epracownicy.setDisable(true);
            zdata.setDisable(true);
            edania.setDisable(true);
            saveButton.setVisible(false);
            try{
                String str = "SELECT pesel FROM hotel_pracownicy WHERE upper(nazwisko) = upper(\'" + epracownicy.getValue() + "\')";
                PreparedStatement stmt = zamowienia.dataBase.getCon().prepareStatement(str);
                ResultSet rs = stmt.executeQuery();
                rs.next();
                String temp_pesel = rs.getString("pesel");
                rs.close();
                stmt.close();
                CallableStatement cstmt = zamowienia.dataBase.getCon().prepareCall("{? = call dodajZamowienie(?,?,?,1)}");
                cstmt.registerOutParameter(1, Types.INTEGER);
                cstmt.setString(2, id);
                cstmt.setDate(3, Date.valueOf(zdata.getValue()));
                cstmt.setString(4, temp_pesel);
                cstmt.execute();
                cstmt.close();
                str = "DELETE FROM hotel_zamowienie_dania where zamowienie=" + id;
                stmt = zamowienia.dataBase.getCon().prepareStatement(str);
                stmt.executeQuery();
                stmt.close();
                for(String i : dania){
                    cstmt = zamowienia.dataBase.getCon().prepareCall("{call dodajZamowienieDania(?,?)}");
                    str = "SELECT * FROM hotel_dania WHERE upper(nazwa)=upper(\'" + i + "\')";
                    stmt = zamowienia.dataBase.getCon().prepareStatement(str);
                    rs = stmt.executeQuery();
                    rs.next();
                    cstmt.setInt(1, Integer.parseInt(id));
                    cstmt.setInt(2, rs.getInt("id_dania"));
                    cstmt.execute();
                }
                cstmt.close();
            }catch (Exception ex){
//                ex.printStackTrace();
            }
        }else if(checkCorrectness()){
            int idDania = 0;
            String pesel = null;
            try {
                String str = "SELECT * FROM hotel_dania WHERE nazwa=\'" + edania.getSelectionModel().getSelectedItem() + "\'";
                PreparedStatement stmt = zamowienia.dataBase.getCon().prepareStatement(str);
                ResultSet rs = stmt.executeQuery();
                rs.next();
                idDania = rs.getInt("id_dania");
                stmt.close();
                str = "SELECT * FROM hotel_pracownicy WHERE nazwisko=\'" + epracownicy.getSelectionModel().getSelectedItem() + "\'";
                stmt = zamowienia.dataBase.getCon().prepareStatement(str);
                rs = stmt.executeQuery();
                rs.next();
                pesel = rs.getString("pesel");
                rs.close();
                stmt.close();

            } catch (SQLException ex) {
//                ex.printStackTrace();
            }
            zamowienia.dataBase.addZamowienie(Date.valueOf(zdata.getValue()), pesel, dania);
            returnTo();
        }
    }
    @FXML
    private void edit(){
        if(!look)
            return;
        edit = true;
        epracownicy.setDisable(false);
        zdata.setDisable(false);
        edania.setDisable(false);
        saveButton.setVisible(true);
    }
    @FXML
    private void delete(){
        try {
            String str = "DELETE FROM hotel_zamowienia WHERE id_zamowienia=" + id;
            PreparedStatement stmt = zamowienia.dataBase.getCon().prepareStatement(str);
            stmt.executeQuery();
            stmt.close();
            returnTo();
        } catch (Exception ex) {
//            ex.printStackTrace();
        }

    }

    private boolean checkCorrectness(){
        boolean correct = true;
        if (epracownicy.getValue() == null){
            correct = false;
            epracownicy.getStyleClass().add("wrong");
            epracownicy.setTooltip(new Tooltip("Trzeba wybrać pracownika"));
        }else{
            while (epracownicy.getStyleClass().remove("wrong"));
            epracownicy.setTooltip(null);
        }
        if (zdata.getValue() == null || zdata.getValue().toString().matches("^((0[1-9]|[12]\\d|3[01])-(0[1-9]|1[0-2])-[12]\\d{3})$")){
            correct = false;
            zdata.getStyleClass().add("wrongDate");
            while (zdata.getStyleClass().remove("addDate"));
            zdata.setTooltip(new Tooltip("Niepoprawna data"));
        }else{
            while (zdata.getStyleClass().remove("wrongDate"));
            zdata.getStyleClass().add("addDate");
            zdata.setTooltip(null);
        }
        if (daniaScroll.getChildren().isEmpty()){
            correct = false;
            edania.getStyleClass().add("wrong");
            edania.setTooltip(new Tooltip("Trzeba wybrać danie"));
        }else{
            while (edania.getStyleClass().remove("wrong"));
            edania.setTooltip(null);
        }
        return correct;
    }

    @FXML
    private void chooseDanie(){
        if(edania.getValue() == null)
            return;
        String temp_danie = edania.getValue();
//        try {
//            String str = "SELECT * FROM hotel_dania WHERE upper(nazwa)=upper(\'" + temp_danie + "\')";
//            PreparedStatement stmt = zamowienia.dataBase.getCon().prepareStatement(str);
//            ResultSet rs = stmt.executeQuery();
//            rs.next();
        if(!dania.contains(temp_danie)){
            dania.add(temp_danie);
            daniaScroll.getChildren().add(createDanieButton(temp_danie));
        }
//            rs.close();
//            stmt.close();
//        }catch(SQLException ex) {
////            ex.printStackTrace();
//        }

    }

    private Button createDanieButton(String nazwa) {
        Button button = new Button(nazwa);
        button.getStyleClass().add("danieButton");
        button.setOnAction(e -> deleteButton(button));
        return button;
    }
    private void deleteButton(Button toDelete){
        if(!edit)
            return;
        daniaScroll.getChildren().remove(toDelete);
        dania.clear();
        try {
            for (Node i : daniaScroll.getChildren()) {
                String temp_danie = ((Button) i).getText();
                String str = "SELECT * FROM hotel_dania WHERE upper(nazwa)=upper(\'" + temp_danie + "\')";
                PreparedStatement stmt = zamowienia.dataBase.getCon().prepareStatement(str);
                ResultSet rs = stmt.executeQuery();
                rs.next();
                dania.add(rs.getString("nazwa"));
                rs.close();
                stmt.close();
            }
        }catch (Exception ex){
//            ex.printStackTrace();
        }
    }
    @FXML
    private void returnTo(){
        controller.changeScene("main_view.fxml", zamowienia);
    }

    private void getDania(){
        try {
            String str = "select d.nazwa, d.id_dania, z.*, p.nazwisko from hotel_pracownicy p inner join hotel_zamowienia z on (p.pesel=z.pracownik) inner join hotel_zamowienie_dania zd on(zd.zamowienie=z.id_zamowienia) inner join hotel_dania d on(zd.id_dania=d.id_dania) where z.id_zamowienia=" + id;
            PreparedStatement stmt = zamowienia.dataBase.getCon().prepareStatement(str);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                dania.add(rs.getString("nazwa"));
            }
            rs.close();
            stmt.close();
        }catch(SQLException ex) {
//            ex.printStackTrace();
        }
    }

}
