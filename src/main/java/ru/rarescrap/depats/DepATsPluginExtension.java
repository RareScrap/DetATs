package ru.rarescrap.depats;

import org.gradle.api.Project;

import java.io.File;

public class DepATsPluginExtension {
    // Можно без проверок на null. Если null, то получим TaskValidationException (или это только для конфигаруции таска?)
    private String ATsDependenciesFilePath = "";// = getProject().getRootProject().getBuildDir() + "\\dependencies_at.cfg";

    public File getATsFile() {
        return new File(ATsDependenciesFilePath);
    }

    public String getDepATsPath() {
        return ATsDependenciesFilePath;
    }

    public void setDepATsPath(String message) {
        this.ATsDependenciesFilePath = message;
    }

    void initDefault(Project project) {
        ATsDependenciesFilePath = project.getRootProject().getBuildDir() + "\\dependencies_at.cfg";
    }

}
