package ru.rarescrap.depats;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class BasePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        DepATsPluginExtension e = project.getExtensions().create("DepATs", DepATsPluginExtension.class, project);
        GetDepATsTask t = project.getTasks().create("getDepATs", GetDepATsTask.class);
        //t.dependsOn("deobfuscateJar"); // TODO: Протестировать
    }
}