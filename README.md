# MoodCat.me

[![Build Status](https://travis-ci.org/MoodCat/MoodCat.me-Core.svg?branch=master)](https://travis-ci.org/MoodCat/MoodCat.me-Core)

**This project is discontinued (for now).**

# Installation instructions

To succesfully run/test the application, a couple of steps should be followed to install various dependencies and configurations:

1. After cloning, open the IDE of your preference (Eclipse/IntelliJ), import the project as maven project and make sure `target/metamodel` is a source-folder of your project.
2. Execute the following Maven commands:
  * `mvn generate-resources` This command should create various classes in `target/metamodel`
  * `mvn install` This command should install all maven dependencies
3. Install Lombok
  * (Eclipse only) Close your IDE and run the following jar. `.m2/repository/org/projectlombok/lombok/LATEST_LOMBOK_VERSION/lombok-LATEST_LOMBOK_VERSION.jar`
The installation will provide the steps to attach Lombok to your IDE.
  * (IntelliJ only) Go to `preferences->plugins->browse repositories->lombok` and install the plugin.
4. (Production only) Whenever you want to run the App in production, the database connection requires a password. Therefore it is not possible to launch if you don't add this password to the environment:
    * (Eclipse only) Go to `run->run configurations->Java Application->App`.
Click the `Environment` tab.
    * (IntelliJ only) Go to `me.moodcat.core.App` and click on `edit configurations`.
  * Click on `new`.
As name set `database-password` and as value the database password.
Contact MoodCat if you want to get access to the database.

If you don't have the password, you can always run the `src/test/java/TestPackageAppRunner` which will use a dummy (embedded H2) database.

# Building and running the server
```sh
mvn package -DskipTests
export Database_Password=#yourdatabasepassword
java -jar target/moodcat-core-distribution/moodcat-core/moodcat-core.jar
```

The frontend files should be provided either using a proxy server (`grunt serve`) or by placing the frontend sources under the `static/app` folder.

# Delomboked sources
We use [Lombok](https://projectlombok.org) for code generation.
Delombokked sources are generated using the `mvn  lombok:delombok` under `target/generated-sources/delombok`.
These sources can be used for static analysis and Javadoc generation.
