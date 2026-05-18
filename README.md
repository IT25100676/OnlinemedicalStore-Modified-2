# OnlinemedicalStore
medical store

## Run on Windows

```bat
mvnw.cmd spring-boot:run

# if  does not work this command try  this....
.\mvnw.cmd spring-boot:run
```

If port 8080 is already in use:

```bat
mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=8082
```

The app opens at `http://localhost:8080` by default, or at the port you pass in the command.

## Management module folders

Each admin management module has a branch handoff folder under `management-modules/`:

- `user-management`
- `medicine-management`
- `order-management`
- `prescription-management`
- `payment-management`
- `feedback-review-management`

These folders document the source files, templates, data files, and CRUD coverage for each module so they can be developed in separate GitHub branches and merged back into `main`.
