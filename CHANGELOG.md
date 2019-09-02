# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2019-09-02
### Added
- Ищет трансормеры во всех зависимостях
- По умолчанию игноририрует forge и fml трансформеры, которые и так применяются. Есть возможность игнорировать любые другие трансформеры при помощи свойства `ignoredATs` в блоке `DepAts`.
- В итоговом файле помечаются названия трансформеров, зависимость откуда они извлечены, а так же пропущенные трансформеры, которые не имею уникальных элементов
- Возможность получить полный лог работы через ключ `-info`
- Возможность указания собственного файла, куда будут складироваться трансформемы:  при помощи свойства `depATs` в блоке `DepAts`