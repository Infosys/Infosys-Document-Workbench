# Angular

~~This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 1.7.1.~~ **`DOCWB: Upgraded to Angular CLI: 12.2.18`**

## `DOCWB: Customization`

### Install `pnpm` package manager 

```dos
npm install -g pnpm@8.5.1
```

### Set `pnpm` as preferred pacakge manager for Angular

```dos
ng config -g cli.packageManager pnpm
```

### Project installation  

```dos 
pnpm install
```

## Development server

Run ~~`ng serve`~~ `npm run start ` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

To update `tenantId` value in `config-data.json` prior to build, run below command.

```json
{
    "config": {
        "tenantId": "",
    }
}
```

```bash
node .\update-config.js "config-data.json" "ae30c578-8569-4f86-be17-642ebaef2e52"
node .\update-config.js "conf/test/config-data.json" "ae30c578-8569-4f86-be17-642ebaef2e52"
```

Run ~~`ng build`~~ `npm run build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
