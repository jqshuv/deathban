# DeathBan
DeathBan is a very simple and lightweigt plugin which bans a player when he dies.

**Any ideas or suggestions? Join the discord!**  

[![discord](https://img.shields.io/discord/903750807957147718?color=7289da&label=discord&logo=discord&logoColor=white)](https://discord.com/invite/BeYVQ3fhF4)

### Configuration

```yaml
settings:
  banreason: "Game Over!" # Here you set the ban reason which is shown in the ban screen.
  ban-delay: 60 # Seconds after death till user is banned. (Can be 0 for instant ban)
  spectator-after-death: true # If the user get gamemode spectator after death.
  ban-time: 30 # Ban time in minutes or 0 for permanent.
  ban-ip: false # true or false, if the user should be IP banned.
  ignore-permission: false # true or false, if the users with permission should be ignored by the plugin.
```

### Permissions

| Permission      |            Description            |
|-----------------|:---------------------------------:|
| deathban.immune | Gives you immunity to death bans. |
