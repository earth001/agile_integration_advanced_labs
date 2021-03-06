package com.redhat.training.gpte.route;

import static org.apache.camel.LoggingLevel.INFO;

import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.spi.DataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FromCsvToJsonRoute extends RouteBuilder {

  @Autowired
  DataFormat df;
  @Autowired
  Endpoint dozer;


  @Override
  public void configure() throws Exception {
    //@formatter:off
    //Marshal exception handling
    onException(IllegalArgumentException.class)
      .maximumRedeliveries(0).handled(true)
      .log(INFO, "%% marshal-exception handled.").to("file://src/data/error?fileName=csv-record-${date:now:yyyyMMdd}.txt");
    //Data format handler
//    BindyCsvDataFormat df = new BindyCsvDataFormat(Customer.class);
    //From CSV to Customer
    from("file://src/data/inbox?fileName=customers.csv&noop=true")
      .to("direct:inputFile");
    from("direct:inputFile")
      .convertBodyTo(String.class)
      .log(">> Reading ${file:onlyname} : ${body}")
      .split(bodyAs(String.class).tokenize("\n"))
      .log(">> Processing line : ${body}")
      .unmarshal(df)
      .log(">> Parsing line to Customer : ${body}")
      .to("direct:csv2json");
    //From Customer to Json
    from("direct:csv2json")
    .to("dozer:csv2json2?mappingFile=transformation.xml&targetModel=org.globex.Account")
    //.to(dozer)
    .log(">> Transformation Customer to Account ${body}")
      .marshal().json(JsonLibrary.Jackson)
      .log(">> Account JSON : ${body}");
      //.to("file://src/data/outbox?fileName=account-${property.CamelSplitIndex}.json");
    //@formatter:on
  }


}
