### Property service

Request organisation properties via http, using following formats:

- `/<property_key>` to get value by <property_key>
- `/env/<env_property_key>` prepend with /env for environment properties
- `/<property_key_1>,<property_key_2>` for multiple properties
- `/json/<property_key_1>,<property_key_2>` or `/<property_key_1>,<property_key_2>?json` for json format

### Additional endpoints

- `/info` or `/version` for build info
- `/health` for health status checks