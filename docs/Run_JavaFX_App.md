# How to Run the JavaFX Application

The main entry point for the GUI in this project is `com.sac.GUIApp`. There are multiple easy ways to run the project.

## Option 1: Running from the Command Line (Recommended)

I've configured the `javafx-maven-plugin` in your `pom.xml`, so you can quickly compile and run the application using Maven directly from your terminal.

1. Open your terminal in the root directory where the `pom.xml` file is located (`d:\Smart Assignment Checker (SAC) A Multi-Layer Plagiarism Detection System`).
2. Run the following command:
   ```bash
   mvn clean compile javafx:run
   ```
This will automatically compile your code, resolve the JavaFX dependencies, and launch the Dashboard.

## Option 2: Running via Your IDE (IntelliJ, VS Code, Eclipse)

You can also run the application natively by utilizing the run execution tools provided by most modern Java IDEs.

### For IntelliJ IDEA, Eclipse or NetBeans:
1. Right-click your `pom.xml` and choose **Reload Project** (or **Update Maven Project** for Eclipse) to ensure the newly added JavaFX dependencies are synced.
2. In the Project Explorer pane, locate `src/main/java/com/sac/GUIApp.java`.
3. Open `GUIApp.java` and click on the green **Run** / **Play** arrow located in the gutter next to the `public static void main(String[] args)` method.
    * _Tip: If the IDE complains that JavaFX components are missing, ensure your Project SDK is set to Java 17 in the settings and that Maven is fully synced._

### For Visual Studio Code:
1. Make sure you have the **Extension Pack for Java** extension installed.
2. Allow the IDE to completely sync the project dependencies.
3. Open `src/main/java/com/sac/GUIApp.java`.
4. Right above the `main` method, you will see a small inline `Run | Debug` lens. Click **Run**.
