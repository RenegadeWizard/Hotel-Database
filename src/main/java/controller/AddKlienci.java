package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.sql.*;

public class AddKlienci {
    private Controller controller;
    private Object toReturnTo;
    private String peselFilled;
    private String fxml;
    private boolean look;
    @FXML
    private Button saveButton, editButton, delButton;
    @FXML
    private TextField imie, nazwisko, pesel, numerTel, adresZa;

    public AddKlienci(Controller controller, Object toReturnTo, String peselFilled, String fxml, boolean look){
        this.controller = controller;
        this.toReturnTo = toReturnTo;
        this.peselFilled = peselFilled;
        this.fxml = fxml;
        this.look = look;
    }
    @FXML
    private void initialize(){
        pesel.setText(peselFilled);// Pesel jest w taki sposób bo jak przejdziemy z rezerwacji
        // do dodawania klientów to go tu automatycznie uzupełniamy
        if(look){
            try {
                String str = "SELECT * FROM hotel_klienci WHERE pesel=\'" + peselFilled + "\'";
                PreparedStatement stmt = ((Klienci)toReturnTo).dataBase.getCon().prepareStatement(str);
                ResultSet rs = stmt.executeQuery();
                rs.next();
                saveButton.setVisible(false);
                imie.setText(rs.getString("imie"));
                imie.setEditable(false);
                nazwisko.setText(rs.getString("nazwisko"));
                nazwisko.setEditable(false);
                pesel.setEditable(false);
                numerTel.setText(rs.getString("numer_telefonu"));
                numerTel.setEditable(false);
                adresZa.setText(rs.getString("adres_zamieszkania"));    // TODO
                adresZa.setEditable(false);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }else{
            editButton.setVisible(false);
            delButton.setVisible(false);
        }
    }
    @FXML
    private void returnTo(){
        controller.changeScene(fxml, toReturnTo);
    }
    @FXML
    private void addKlienci(){
        if(look && checkCorrectness()) {
            saveButton.setVisible(false);
            imie.setEditable(false);
            nazwisko.setEditable(false);
            pesel.setEditable(false);
            numerTel.setEditable(false);
            adresZa.setEditable(false);
            try{
                CallableStatement cstmt = ((Klienci) toReturnTo).dataBase.getCon().prepareCall("{call dodajKlienta(?,?,?,?,?,1)}");
                cstmt.setString(1, pesel.getText());
                cstmt.setString(2, imie.getText());
                cstmt.setString(3, nazwisko.getText());
                cstmt.setString(4, numerTel.getText());
                cstmt.setString(5, adresZa.getText());
                cstmt.execute();
                cstmt.close();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }else if(checkCorrectness()) {
            if (toReturnTo.getClass() == Klienci.class) {
                ((Klienci) toReturnTo).dataBase.addKlienci(imie.getText(), nazwisko.getText(),
                        pesel.getText(), numerTel.getText(), adresZa.getText());
                returnTo();
            }
            else{
                ((AddRezerwacje) toReturnTo).dataBase.addKlienci(imie.getText(), nazwisko.getText(),
                        pesel.getText(), numerTel.getText(), adresZa.getText());
                returnTo();
            }
        }else{
        }
    }
    @FXML
    private void edit() {
        if (!look)
            return;
        saveButton.setVisible(true);
        imie.setEditable(true);
        nazwisko.setEditable(true);
        pesel.setEditable(false);
        numerTel.setEditable(true);
        adresZa.setEditable(true);
    }
    @FXML
    private void delete(){
        try {
            String str = "DELETE FROM hotel_klienci WHERE pesel=\'" + peselFilled + "\'";
            PreparedStatement stmt = ((Klienci) toReturnTo).dataBase.getCon().prepareStatement(str);
            stmt.executeQuery();
            stmt.close();
            returnTo();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private boolean checkCorrectness(){
        boolean correct = true;
        if (imie.getText().isEmpty() || imie.getText().length() > 30){
            correct = false;
            imie.getStyleClass().add("wrong");
        }else{
            while (imie.getStyleClass().remove("wrong"));
        }
        if (nazwisko.getText().isEmpty() || nazwisko.getText().length() > 30){
            correct = false;
            nazwisko.getStyleClass().add("wrong");
        }else{
            while (nazwisko.getStyleClass().remove("wrong"));
        }
        if (!pesel.getText().matches("[0-9]{11}")){
            correct = false;
            pesel.getStyleClass().add("wrong");
        }else{
            while (pesel.getStyleClass().remove("wrong"));
        }
        if (!numerTel.getText().matches("[0-9]{9}")){
            correct = false;
            numerTel.getStyleClass().add("wrong");
        }else{
            while (numerTel.getStyleClass().remove("wrong"));
        }
        if (adresZa.getText().isEmpty() || adresZa.getText().length() > 50){
            correct = false;
            adresZa.getStyleClass().add("wrong");
        }else{
            while (adresZa.getStyleClass().remove("wrong"));
        }
        return correct;
    }

}
