# PinkBull ![Spiget Version](https://img.shields.io/spiget/version/102050?label=Latest%20version) [![CodeQL](https://github.com/velnias75/PinkBull/actions/workflows/codeql.yml/badge.svg)](https://github.com/velnias75/PinkBull/actions/workflows/codeql.yml)
<img src="https://github.com/velnias75/PinkBull/raw/master/icon.svg" height="64px"> **PinkBull verleiht Flügel! / PinkBull gives you wings.**

Tiny plugin to get a temporary fly effect by crafting a potion.
The duration of the effect is configurable (defaults to 15 minutes).

Languages English or German.

**Minecraft version `1.17+`**

## Recipe
![Screenshot_20221002_082640](https://user-images.githubusercontent.com/4481414/193441624-c203fc41-0066-454d-b07d-bccac3016613.png)

* 5 Sugar
* 1 Water bucket (you'll get back an empty bucket)
* 3 Magma cream

## Commands
* `/pinkbull [<duration>]` gives you a **free** PinkBull with *optional duration in seconds* *(permission: pinkbull.pinkbull)*
* `/fly [<player>]` enables you or `player` to just **fly** unlimited *(permission: pinkbull.fly)*

### Building
After *checkout/clone* do
`git submodule update --init --recursive` once, than `./gradlew clean build`.

---

![](https://bstats.org/signatures/bukkit/PinkBull.svg)

