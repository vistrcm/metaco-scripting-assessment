The target of the below exercise is to evaluate your ability to monitor a system.
The exercise has been calibrated to take up to 2h, but you may decide not to do the whole exercise.
When submitting your exercise, please make sure to fill the feedback.md file

If you feel questions are not precise enought, feel free to make your own assumptions and write your assumptions in the feedback document.

The test has 2 parts which do not depend on each other. If you have trouble with the first part, feel free to jump to the seconde one. We recommend you to start with the part you are the most comfortable with.

# Test Scenario

As part of your job, you have to monitor an online blockchain ethereum node.

The full API reference of eth node can be found [here](https://ethereum.org/en/developers/docs/apis/json-rpc/#json-rpc-methods).

The whole Grafana stack components are in the `docker-compose.yaml` file.

To start the whole stack, you can simply run `docker compose up --force-recreate`


# PART 1: Creating a Probe to collect last block from an ETH node

The first part of this test has been designed to test your coding ability.
ETH Node is not compatible with the OpenTelemetryProtocol standard (OTLP).


You are asked to code a probe, with the technology of your choice, to continuously pull the last block from the eth node and push them as a metric into the OTLP protocol every 5 seconds.

You should put all your code in the `your-probe` folder, where there is already a typescript boilerplate.
If you are not comfortable with typesecript, feel free to use the language of your choice.

To get the latest block from the eth node, you will need a single rest call to the ETH node.
This call is documented as a curl below:
```
curl --location 'https://ethereum.publicnode.com' \
--header 'Content-Type: application/json' \
--data '{
    "jsonrpc": "2.0",
    "method": "eth_getBlockByNumber",
    "params": ["latest",false],
    "id": 1
}'
```

and the data of interest in that response call is under `nodeBody.result.number`, encoded in hexa.

In addition to the curl, a typescript code is also provided in the `your-probe/src/client.ts`.


To run probe with monitoring use:
```shell
docker compose -f docker-compose-app.yaml up --force-recreate --build
```

# PART 2: Grafana monitoring

The first part of this test has been designed to test your monitoring ability.
In the docker compose stack, the component `k6-tracing` generate random traces.

Use these traces to create a dashboard that shows the average latency of adding an article to a cart (`PUT /article-to-cart`) and the number of hit of that endpoint over the last 10 minutes.

