# How to Run the JavaFX Application

cmd for compile
 javac --module-path "C:\javafx\lib" --add-modules javafx.controls,javafx.fxml src\main\java\com\sac\*.java 

cmd for run
 java --module-path "C:\javafx\lib" --add-modules javafx.controls,javafx.fxml -cp "target\classes;src\main\resources" com.sac.GUIApp