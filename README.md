# 💀 DeathBan

A lightweight and simple plugin that bans players when they die. Perfect for hardcore survival servers!

**Got ideas or suggestions? Join our Discord!**  

[![discord](https://img.shields.io/discord/903750807957147718?color=7289da&label=discord&logo=discord&logoColor=white)](https://discord.com/invite/BeYVQ3fhF4)

---

## ✨ Features

- 🎮 **Simple & Lightweight** - Minimal resource usage
- 🔥 **Folia Support** - Works on modern Folia servers
- ⚔️ **PvP Mode** - Ban only when killed by other players
- 🎨 **Colorful Ban Messages** - Support for Adventure API with colors and formatting
- ⏱️ **Flexible Timing** - Customizable ban delay and duration
- 👁️ **Spectator Mode** - Optional spectator mode after death
- 🔐 **IP Banning** - Choose between player or IP bans

---

## 📋 Configuration

```yaml
settings:
  banreason: "<bold><red>Game Over!</red></bold>\n\n<gray>You were banned for dying in combat.</gray>"
  ban-delay: 3                # Seconds after death until ban
  spectator-after-death: true # Enable spectator mode after death
  ban-time: 30                # Ban duration in minutes (0 = permanent)
  ban-ip: false               # Ban IP address instead of player name
  ignore-permission: false    # Ignore deathban.immune permission
  player-kill-only: false     # Only ban when killed by other players
  debug: false                # Enable debug logging
```

### Color & Format Support

The `banreason` field supports Adventure API MiniMessage formatting:

**Colors:** `<red>`, `<blue>`, `<green>`, `<yellow>`, `<aqua>`, `<light_purple>`, `<gold>`, `<gray>`

**Formatting:** `<bold>`, `<italic>`, `<underlined>`, `<strikethrough>`

**Example:**
```yaml
banreason: "<bold><red>BANNED!</red></bold>\n<yellow>Reason: <gray>Died in combat</gray></yellow>"
```

---

## 🔑 Permissions

| Permission      | Description                     |
|-----------------|---------------------------------|
| `deathban.immune` | Immunity from death bans        |

---

## 📝 Changelog

### Version 1.3.0
- ✅ Folia Support
- ✅ Player-Kill-Only Mode
- ✅ Better Ban Messages (Adventure API)
- ✅ Debug Mode
- ✅ Ban Timing Fixed
- ✅ Safer Teleportation

---

## 🚀 Installation

1. Download the latest `DeathBan.jar`
2. Place it in your `plugins` folder
3. Restart your server
4. Configure `plugins/DeathBan/config.yml` to your needs

**Enjoy! 💀**

