#### Infy Docwb Search Service


Please put the config file accoring to the use in VM or in local. in case of local use put the config/local/config.ini to local/config.ini.


This is for deploying the service file

```
sudo systemctl status infy_docwb_search_service.service
```


infy_docwb_search_service.service - Infy Docucment DX Servie (Dev)
Loaded: loaded (/etc/systemd/system/infy_docwb_search_service.service; enabled; vendor preset: disabled)
Active: active (running) since Tue 2023-05-09 09:05:44 IST; 2 days ago


```
sudo systemctl stop infy_docwb_search_service.service
sudo systemctl status infy_docwb_search_service.service
```


infy_docwb_search_service.service - Infy Docucment DX Servie (Dev)
Loaded: loaded (/etc/systemd/system/infy_docwb_search_service.service; enabled; vendor preset: disabled)
Active: inactive (dead) since Thu 2023-05-11 14:51:26 IST; 4s ago


```
sudo systemctl disable infy_docwb_search_service.service
```

Removed symlink /etc/systemd/system/multi-user.target.wants/infy_docwb_search_service.service.
