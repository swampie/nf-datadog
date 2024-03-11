# Datadog Plugin 
 
This project contains a simple Nextflow plugin which provides integration with Datadog:

## Roadmap
- [x] sends all tasks log (on success or on failure)
- [ ] add tests
- [ ] sends metrics per tasks
- [ ] sends traces 

## How to use it
Launch a pipeline with the following configuration in `nextflow.config`:

```groovy
datadog {
    apiKey='<the api key from datadog dashboard>',
    site='<the site domain of the datadog account'
}
```

Additionally the following parameter can be used

| Parameter | Description                                                                     | Default Value |
|-----------|---------------------------------------------------------------------------------|---------------|
| maxLines  | The maximum number of lines to fetch from the logs (out or error) for each task | 50            |
| service   | The service identifier specified when the logs are sent to datadog              | nextflow      |


