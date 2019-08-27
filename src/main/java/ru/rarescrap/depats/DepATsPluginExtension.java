package ru.rarescrap.depats;

import org.gradle.api.Project;

import java.io.File;

public class DepATsPluginExtension {
    /** Местонахождение файла, который будет использоваться для хранения трансформеров из зависимостей */
    private File depATs;

    public DepATsPluginExtension(Project project) {
        depATs = new File(project.getBuildDir().getAbsolutePath() + File.separator + "dependencies_at.cfg");
    }

    public File getDepATs() {
        return depATs;
    }

    public void setDepATs(File depATs) {
        this.depATs = depATs;
    }
}
