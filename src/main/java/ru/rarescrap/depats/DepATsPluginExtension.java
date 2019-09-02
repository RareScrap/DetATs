package ru.rarescrap.depats;

import org.gradle.api.Project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DepATsPluginExtension {
    /** Местонахождение файла, который будет использоваться для хранения трансформеров из зависимостей */
    private File depATs;
    /** Игнорируемые трансформеры в формате "имя_cfg_файла:имя_jar'ника_зависимости".
     * Или просто "имя_cfg_файла", но в этом случае будут игнорироваться cfg-файлы из всех
     * зависимостей с таким названием */
    List<String> ignoredATs = new ArrayList<>();

    public DepATsPluginExtension(Project project) {
        // Дефолтные значения
        depATs = new File(project.getBuildDir().getAbsolutePath() + File.separator + "dependencies_at.cfg");
        ignoredATs.add("forge_at.cfg");
        ignoredATs.add("fml_at.cfg");
    }

    public static DepATsPluginExtension get(Project project) {
        return project.getExtensions().getByType(DepATsPluginExtension.class);
    }

    public File getDepATs() {
        return depATs;
    }

    public void setDepATs(File depATs) {
        this.depATs = depATs;
    }
}
