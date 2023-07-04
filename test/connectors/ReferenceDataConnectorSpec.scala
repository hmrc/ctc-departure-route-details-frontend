/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import base.{AppWithDefaultMockFixtures, SpecBase, WireMockServerHandler}
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import models.reference._
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import play.mvc.Http.HeaderNames.CONTENT_TYPE
import play.mvc.Http.MimeTypes.JSON
import play.mvc.Http.Status._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec extends SpecBase with AppWithDefaultMockFixtures with WireMockServerHandler with ScalaCheckPropertyChecks {

  private val baseUrl = "customs-reference-data/test-only"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder = super
    .guiceApplicationBuilder()
    .configure(
      conf = "microservice.services.customsReferenceData.port" -> server.port()
    )

  private lazy val connector: ReferenceDataConnector = app.injector.instanceOf[ReferenceDataConnector]

  private val customsOfficeDestinationResponseJson: String =
    """
      | {
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/CustomsOffices"
      |    }
      |  },
      |  "meta": {
      |    "version": "410157ad-bc37-4e71-af2a-404d1ddad94c",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "CustomsOffices",
      |  "data": [
      |    {
      |      "state": "valid",
      |      "activeFrom": "2019-01-01",
      |      "id": "GB1",
      |      "name": "testName1",
      |      "LanguageCode": "EN",
      |      "countryId": "GB",
      |      "eMailAddress": "foo@andorra.ad",
      |      "roles": [
      |        {
      |          "role": "DEP"
      |        }
      |      ]
      |    },
      |    {
      |      "state": "valid",
      |      "activeFrom": "2019-01-01",
      |      "id": "GB2",
      |      "name": "testName2",
      |      "LanguageCode": "ES",
      |      "countryId": "GB",
      |      "roles": [
      |        {
      |          "role": "DEP"
      |        }
      |      ]
      |    }
      |  ]
      |}
      |""".stripMargin

  private def countriesResponseJson(listName: String): String =
    s"""
      |{
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/$listName"
      |    }
      |  },
      |  "meta": {
      |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "$listName",
      |  "data": [
      |    {
      |      "activeFrom": "2023-01-23",
      |      "code": "GB",
      |      "state": "valid",
      |      "description": "United Kingdom"
      |    },
      |    {
      |      "activeFrom": "2023-01-23",
      |      "code": "AD",
      |      "state": "valid",
      |      "description": "Andorra"
      |    }
      |  ]
      |}
      |""".stripMargin

  def queryParams(role: String): Seq[(String, StringValuePattern)] = Seq(
    "data.countryId"  -> equalTo("GB"),
    "data.roles.role" -> equalTo(role)
  )

  "Reference Data" - {

    "getCustomsOfficesOfTransitForCountry" - {

      "must return a successful future response with a sequence of CustomsOffices" in {
        server.stubFor(
          get(urlPathMatching(s"/$baseUrl/filtered-lists/CustomsOffices"))
            .withQueryParams(queryParams("TRA").toMap.asJava)
            .willReturn(okJson(customsOfficeDestinationResponseJson))
        )

        val expectedResult = Seq(
          CustomsOffice("GB1", "testName1", None),
          CustomsOffice("GB2", "testName2", None)
        )

        connector.getCustomsOfficesOfTransitForCountry(CountryCode("GB")).futureValue mustBe expectedResult
      }

      "must return a successful future response when CustomsOffice returns no data" in {
        server.stubFor(
          get(urlPathMatching(s"/$baseUrl/filtered-lists/CustomsOffices")).willReturn(
            aResponse()
              .withStatus(NO_CONTENT)
              .withHeader(CONTENT_TYPE, JSON)
          )
        )

        val expectedResult = Nil

        connector.getCustomsOfficesOfTransitForCountry(CountryCode("AR")).futureValue mustBe expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(s"/$baseUrl/filtered-lists/CustomsOffices", connector.getCustomsOfficesOfTransitForCountry(CountryCode("GB")))
      }
    }

    "getCustomsOfficesOfDestinationForCountry" - {
      "must return a successful future response with a sequence of CustomsOffices" in {
        server.stubFor(
          get(urlPathMatching(s"/$baseUrl/filtered-lists/CustomsOffices"))
            .withQueryParams(queryParams("DES").toMap.asJava)
            .willReturn(okJson(customsOfficeDestinationResponseJson))
        )

        val expectedResult = Seq(
          CustomsOffice("GB1", "testName1", None),
          CustomsOffice("GB2", "testName2", None)
        )

        connector.getCustomsOfficesOfDestinationForCountry(CountryCode("GB")).futureValue mustBe expectedResult
      }

      "must return a successful future response when CustomsOffice is not found" in {
        server.stubFor(
          get(urlPathMatching(s"/$baseUrl/filtered-lists/CustomsOffices")).willReturn(
            aResponse()
              .withStatus(NO_CONTENT)
              .withHeader(CONTENT_TYPE, JSON)
          )
        )

        val expectedResult = Nil

        connector.getCustomsOfficesOfDestinationForCountry(CountryCode("AR")).futureValue mustBe expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(s"/$baseUrl/filtered-lists/CustomsOffices", connector.getCustomsOfficesOfDestinationForCountry(CountryCode("GB")))
      }
    }

    "getCountries for full list" - {
      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(s"/$baseUrl/lists/CountryCodesFullList"))
            .willReturn(okJson(countriesResponseJson("CountryCodesFullList")))
        )

        val expectedResult: Seq[Country] = Seq(
          Country(CountryCode("GB"), "United Kingdom"),
          Country(CountryCode("AD"), "Andorra")
        )

        connector.getCountries("CountryCodesFullList").futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(s"/$baseUrl/lists/CountryCodesFullList", connector.getCountries("CountryCodesFullList"))
      }
    }

    "getCustomsSecurityAgreementAreaCountries" - {
      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(s"/$baseUrl/lists/CountryCustomsSecurityAgreementArea"))
            .willReturn(okJson(countriesResponseJson("CountryCustomsSecurityAgreementArea")))
        )

        val expectedResult: Seq[Country] = Seq(
          Country(CountryCode("GB"), "United Kingdom"),
          Country(CountryCode("AD"), "Andorra")
        )

        connector.getCustomsSecurityAgreementAreaCountries().futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(s"/$baseUrl/lists/CountryCustomsSecurityAgreementArea", connector.getCustomsSecurityAgreementAreaCountries())
      }
    }

    "getCountryCodesCTC" - {
      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(s"/$baseUrl/lists/CountryCodesCommonTransit"))
            .willReturn(okJson(countriesResponseJson("CountryCodesCommonTransit")))
        )

        val expectedResult: Seq[Country] = Seq(
          Country(CountryCode("GB"), "United Kingdom"),
          Country(CountryCode("AD"), "Andorra")
        )

        connector.getCountryCodesCTC().futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(s"/$baseUrl/lists/CountryCodesCommonTransit", connector.getCountryCodesCTC())
      }
    }

    "getAddressPostcodeBasedCountries" - {
      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(s"/$baseUrl/lists/CountryAddressPostcodeBased"))
            .willReturn(okJson(countriesResponseJson("CountryAddressPostcodeBased")))
        )

        val expectedResult: Seq[Country] = Seq(
          Country(CountryCode("GB"), "United Kingdom"),
          Country(CountryCode("AD"), "Andorra")
        )

        connector.getAddressPostcodeBasedCountries().futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(s"/$baseUrl/lists/CountryAddressPostcodeBased", connector.getAddressPostcodeBasedCountries())
      }
    }

    "getCountriesWithoutZip" - {
      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(s"/$baseUrl/lists/CountryWithoutZip"))
            .willReturn(okJson(countriesResponseJson("CountryWithoutZip")))
        )

        val expectedResult: Seq[CountryCode] = Seq(
          CountryCode("GB"),
          CountryCode("AD")
        )

        connector.getCountriesWithoutZip().futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(s"/$baseUrl/lists/CountryWithoutZip", connector.getCountriesWithoutZip())
      }
    }
  }

  private def checkErrorResponse(url: String, result: => Future[_]): Assertion = {
    val errorResponses: Gen[Int] = Gen
      .chooseNum(400: Int, 599: Int)
      .suchThat(_ != 404)

    forAll(errorResponses) {
      errorResponse =>
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(errorResponse)
            )
        )

        whenReady(result.failed) {
          _ mustBe an[Exception]
        }
    }
  }

}
