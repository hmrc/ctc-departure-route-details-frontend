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
import models.LocationType
import models.reference._
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import play.mvc.Http.HeaderNames.CONTENT_TYPE
import play.mvc.Http.MimeTypes.JSON
import play.mvc.Http.Status._

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

  private val customsOfficesResponseJson: String =
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

  private val unLocodesResponseJson: String =
    """
      | {
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/UnLocodeExtended"
      |    }
      |  },
      |  "meta": {
      |    "version": "410157ad-bc37-4e71-af2a-404d1ddad94c",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "UnLocodeExtended",
      |  "data": [
      |    {
      |      "state": "valid",
      |      "activeFrom": "2019-01-01",
      |      "unLocodeExtendedCode": "UN1",
      |      "name": "testName1"
      |    },
      |    {
      |      "state": "valid",
      |      "activeFrom": "2019-01-01",
      |      "unLocodeExtendedCode": "UN2",
      |      "name": "testName2"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val specificCircumstanceIndicatorsResponseJson: String =
    """
      | {
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/SpecificCircumstanceIndicatorCode"
      |    }
      |  },
      |  "meta": {
      |    "version": "410157ad-bc37-4e71-af2a-404d1ddad94c",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "SpecificCircumstanceIndicatorCode",
      |  "data": [
      |    {
      |      "state": "valid",
      |      "activeFrom": "2019-01-01",
      |      "code": "SCI1",
      |      "description": "testName1"
      |    },
      |    {
      |      "state": "valid",
      |      "activeFrom": "2019-01-01",
      |      "code": "SCI2",
      |      "description": "testName2"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val locationTypesResponseJson: String =
    """
      |
      |   {
      |   "data": [
      |              {
      |                "type": "A",
      |                "description": "Designated location"
      |              },
      |              {
      |                "type": "B",
      |                "description": "Authorised place"
      |               }
      |            ]
      |   }
      |""".stripMargin

  def queryParams(role: String): Seq[(String, StringValuePattern)] = Seq(
    "data.countryId"  -> equalTo("GB"),
    "data.roles.role" -> equalTo(role)
  )

  "Reference Data" - {

    "getTypeOfLocation" - {
      val url = s"/$baseUrl/lists/TypeOfLocation"
      "must return Seq of security types when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(locationTypesResponseJson))
        )

        val expectedResult: Seq[LocationType] = Seq(
          LocationType("A", "Designated location"),
          LocationType("B", "Authorised place")
        )

        connector.getTypesOfLocation.futureValue mustEqual expectedResult
      }

      "should handle a 204 response for location types" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(aResponse().withStatus(NO_CONTENT))
        )

        connector.getTypesOfLocation.futureValue mustBe Nil
      }

      "should handle client and server errors for control types" in {
        checkErrorResponse(url, connector.getTypesOfLocation())
      }
    }

    "getCustomsOfficesOfTransitForCountry" - {
      def url(countryId: String) = s"/$baseUrl/filtered-lists/CustomsOffices?data.countryId=$countryId&data.roles.role=TRA"

      "must return a successful future response with a sequence of CustomsOffices" in {
        val countryId = "GB"

        server.stubFor(
          get(urlEqualTo(url(countryId)))
            .willReturn(okJson(customsOfficesResponseJson))
        )

        val expectedResult = Seq(
          CustomsOffice("GB1", "testName1", None),
          CustomsOffice("GB2", "testName2", None)
        )

        connector.getCustomsOfficesOfTransitForCountry(CountryCode(countryId)).futureValue mustBe expectedResult
      }

      "must return a successful future response when CustomsOffice returns no data" in {
        val countryId = "AR"

        server.stubFor(
          get(urlEqualTo(url(countryId))).willReturn(
            aResponse()
              .withStatus(NO_CONTENT)
              .withHeader(CONTENT_TYPE, JSON)
          )
        )

        val expectedResult = Nil

        connector.getCustomsOfficesOfTransitForCountry(CountryCode(countryId)).futureValue mustBe expectedResult
      }

      "must return an exception when an error response is returned" in {
        val countryId = "GB"
        checkErrorResponse(url(countryId), connector.getCustomsOfficesOfTransitForCountry(CountryCode(countryId)))
      }
    }

    "getCustomsOfficesOfDestinationForCountry" - {
      def url(countryId: String) = s"/$baseUrl/filtered-lists/CustomsOffices?data.countryId=$countryId&data.roles.role=DES"

      "must return a successful future response with a sequence of CustomsOffices" in {
        val countryId = "GB"

        server.stubFor(
          get(urlEqualTo(url(countryId)))
            .willReturn(okJson(customsOfficesResponseJson))
        )

        val expectedResult = Seq(
          CustomsOffice("GB1", "testName1", None),
          CustomsOffice("GB2", "testName2", None)
        )

        connector.getCustomsOfficesOfDestinationForCountry(CountryCode(countryId)).futureValue mustBe expectedResult
      }

      "must return a successful future response when CustomsOffice is not found" in {
        val countryId = "AR"

        server.stubFor(
          get(urlEqualTo(url(countryId))).willReturn(
            aResponse()
              .withStatus(NO_CONTENT)
              .withHeader(CONTENT_TYPE, JSON)
          )
        )

        val expectedResult = Nil

        connector.getCustomsOfficesOfDestinationForCountry(CountryCode(countryId)).futureValue mustBe expectedResult
      }

      "must return an exception when an error response is returned" in {
        val countryId = "GB"
        checkErrorResponse(url(countryId), connector.getCustomsOfficesOfDestinationForCountry(CountryCode(countryId)))
      }
    }

    "getCustomsOfficesOfExitForCountry" - {
      def url(countryId: String) = s"/$baseUrl/filtered-lists/CustomsOffices?data.countryId=$countryId&data.roles.role=EXT"

      "must return a successful future response with a sequence of CustomsOffices" in {
        val countryId = "GB"

        server.stubFor(
          get(urlEqualTo(url(countryId)))
            .willReturn(okJson(customsOfficesResponseJson))
        )

        val expectedResult = Seq(
          CustomsOffice("GB1", "testName1", None),
          CustomsOffice("GB2", "testName2", None)
        )

        connector.getCustomsOfficesOfExitForCountry(CountryCode(countryId)).futureValue mustBe expectedResult
      }

      "must return a successful future response when CustomsOffice is not found" in {
        val countryId = "AR"

        server.stubFor(
          get(urlEqualTo(url(countryId))).willReturn(
            aResponse()
              .withStatus(NO_CONTENT)
              .withHeader(CONTENT_TYPE, JSON)
          )
        )

        val expectedResult = Nil

        connector.getCustomsOfficesOfExitForCountry(CountryCode(countryId)).futureValue mustBe expectedResult
      }

      "must return an exception when an error response is returned" in {
        val countryId = "GB"
        checkErrorResponse(url(countryId), connector.getCustomsOfficesOfExitForCountry(CountryCode(countryId)))
      }
    }

    "getCustomsOfficesOfDepartureForCountry" - {
      def url(countryId: String) = s"/$baseUrl/filtered-lists/CustomsOffices?data.countryId=$countryId&data.roles.role=DEP"

      "must return a successful future response with a sequence of CustomsOffices" in {
        val countryId = "GB"

        server.stubFor(
          get(urlEqualTo(url(countryId)))
            .willReturn(okJson(customsOfficesResponseJson))
        )

        val expectedResult = Seq(
          CustomsOffice("GB1", "testName1", None),
          CustomsOffice("GB2", "testName2", None)
        )

        connector.getCustomsOfficesOfDepartureForCountry(countryId).futureValue mustBe expectedResult
      }

      "must return a successful future response when CustomsOffice is not found" in {
        val countryId = "AR"

        server.stubFor(
          get(urlEqualTo(url(countryId))).willReturn(
            aResponse()
              .withStatus(NO_CONTENT)
              .withHeader(CONTENT_TYPE, JSON)
          )
        )

        val expectedResult = Nil

        connector.getCustomsOfficesOfDepartureForCountry(countryId).futureValue mustBe expectedResult
      }

      "must return an exception when an error response is returned" in {
        val countryId = "GB"
        checkErrorResponse(url(countryId), connector.getCustomsOfficesOfDepartureForCountry(countryId))
      }
    }

    "getCountries for full list" - {
      val url = s"/$baseUrl/lists/CountryCodesFullList"

      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countriesResponseJson("CountryCodesFullList")))
        )

        val expectedResult: Seq[Country] = Seq(
          Country(CountryCode("GB"), "United Kingdom"),
          Country(CountryCode("AD"), "Andorra")
        )

        connector.getCountries("CountryCodesFullList").futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getCountries("CountryCodesFullList"))
      }
    }

    "getCustomsSecurityAgreementAreaCountries" - {
      val url = s"/$baseUrl/lists/CountryCustomsSecurityAgreementArea"

      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countriesResponseJson("CountryCustomsSecurityAgreementArea")))
        )

        val expectedResult: Seq[Country] = Seq(
          Country(CountryCode("GB"), "United Kingdom"),
          Country(CountryCode("AD"), "Andorra")
        )

        connector.getCustomsSecurityAgreementAreaCountries().futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getCustomsSecurityAgreementAreaCountries())
      }
    }

    "getCountryCodesCTC" - {
      val url = s"/$baseUrl/lists/CountryCodesCTC"

      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countriesResponseJson("CountryCodesCTC")))
        )

        val expectedResult: Seq[Country] = Seq(
          Country(CountryCode("GB"), "United Kingdom"),
          Country(CountryCode("AD"), "Andorra")
        )

        connector.getCountryCodesCTC().futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getCountryCodesCTC())
      }
    }

    "getAddressPostcodeBasedCountries" - {
      val url = s"/$baseUrl/lists/CountryAddressPostcodeBased"

      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countriesResponseJson("CountryAddressPostcodeBased")))
        )

        val expectedResult: Seq[Country] = Seq(
          Country(CountryCode("GB"), "United Kingdom"),
          Country(CountryCode("AD"), "Andorra")
        )

        connector.getAddressPostcodeBasedCountries().futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getAddressPostcodeBasedCountries())
      }
    }

    "getCountriesWithoutZip" - {
      val url = s"/$baseUrl/lists/CountryWithoutZip"

      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countriesResponseJson("CountryWithoutZip")))
        )

        val expectedResult: Seq[CountryCode] = Seq(
          CountryCode("GB"),
          CountryCode("AD")
        )

        connector.getCountriesWithoutZip().futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getCountriesWithoutZip())
      }
    }

    "getUnLocodes" - {
      val url = s"/$baseUrl/lists/UnLocodeExtended"

      "must return Seq of UN/LOCODES when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(unLocodesResponseJson))
        )

        val expectedResult: Seq[UnLocode] = Seq(
          UnLocode("UN1", "testName1"),
          UnLocode("UN2", "testName2")
        )

        connector.getUnLocodes().futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getUnLocodes())
      }
    }

    "getSpecificCircumstanceIndicators" - {
      val url = s"/$baseUrl/lists/SpecificCircumstanceIndicatorCode"

      "must return Seq of specific circumstance indicators when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(specificCircumstanceIndicatorsResponseJson))
        )

        val expectedResult: Seq[SpecificCircumstanceIndicator] = Seq(
          SpecificCircumstanceIndicator("SCI1", "testName1"),
          SpecificCircumstanceIndicator("SCI2", "testName2")
        )

        connector.getSpecificCircumstanceIndicators().futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getSpecificCircumstanceIndicators())
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
