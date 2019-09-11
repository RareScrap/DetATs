<img src="github_media/logo.png" alt="logo" width="300"/>

# DepATs - Dependency Access Transformers
Плагин для minecraft-моддеров, позволяющий искать Access-трансформеры в зависимостях машего проекта, а затем объединяет их в один файл.

[ ![Download](https://api.bintray.com/packages/rarescrap/minecraft/DepATs/images/download.svg) ](https://bintray.com/rarescrap/minecraft/DepATs/_latestVersion)

## Что такое Access-трансформеры?
В узком смысле, это подход, который использует [Minecraft Forge][1], чтобы моддеры могли получать доступ к приватным полям и методам без необходимости модифицировать `minecraft.jar`. А в широком - процес изменения модификаторов доступа классов на этапе их загрузки.

Подбробнее о транформерах вы можете узнать [тут][3]

## Зачем?
Несмотря на то, что команда MincraftForge не оказывает поддержку устаревшим версиям игры, они все еще развиваются и используются. К сожалению, [ForgeGradle][2] для старых версий не умеет извлекать access-трансформеры из модов-зависимостей [(такое появилось тольков 2.0)][4]. Из-за чего подключать моды через Gradle становится очень неудобно, т.к. вручную приходилсь переписывать все трансформеры в файл. Но с этим плагином вам больше не придется беспокоиться о трансформерах в ваших зависимостях!

## Старт всего за 3 шага
1. Подключите репозиторий с плагином к вашему билдскрипту:
```gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'ru.rarescrap:depATs:1.0.0'
    }
}
```

2. Подключите плагин: (лучше сделать это после включения forge-плагина)
```gradle
apply plugin: 'DepATs'
```

3. Добавьте в блок настроек `minecraft {}` всего одну строку:
```gradle
minecraft {
    // ...
    at(DepATs.getDepATs())
}
```
Теперь запустите `gradlew setupDecompWorkspace` и наслаждайтесь примененными трансформерами.

## Настройки плагина
1. Трансформеры из зависимостей по умолчанию хранятся в `build/dependencies_at.cfg`, но вы можете указать любой файл, какой пожелаете:
```gradle
DepATs {
    depATs = file("myCustomDepATsFile_at.cfg")
}
```
Теперь транформеры будут хранится в корне проекта в файле `myCustomDepATsFile_at.cfg`

2. Вы можете игнорировать cfg-файлы с определенным именем или crf-файлы только для определенной зависимости:
```gradle
DepATs {
    ignoredATs = [
    'ignoreThisFile_at.cfg', // Этот файл будет игнорироваться для всех зависимостей
    'ignoreThoseFile_at.cfg:dependency.jar'] // Файл с этим именем будет игнорироваться только для зависимости dependency.jar
}
```
По умолчанию игнорируются "forge_at.cfg" и "fml_at.cfg", т.к. они применяются и так.

## У меня не рабоает/я нашел баг/у меня есть идея
Вы можете высказать все это на [багтрекере][5]

[1]: https://minecraftforge.net/
[2]: https://github.com/MinecraftForge/ForgeGradle
[3]: https://forum.mcmodding.ru/resources/access-transformers.11/
[4]: https://github.com/MinecraftForge/ForgeGradle/issues/273#issuecomment-150991010
[5]: https://github.com/RareScrap/DetATs/issues
