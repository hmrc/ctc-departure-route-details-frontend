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

import cats.data.NonEmptySet
import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import itbase.{ItSpecBase, WireMockServerHandler}
import models.reference._
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec extends ItSpecBase with WireMockServerHandler with ScalaCheckPropertyChecks {

  private val baseUrl = "customs-reference-data/test-only"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder = super
    .guiceApplicationBuilder()
    .configure(
      conf = "microservice.services.customs-reference-data.port" -> server.port()
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

  private def countryWithoutZipResponseJson: String =
    s"""
       |{
       |  "_links": {
       |    "self": {
       |      "href": "/customs-reference-data/lists/CountryWithoutZip"
       |    }
       |  },
       |  "meta": {
       |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
       |    "snapshotDate": "2023-01-01"
       |  },
       |  "id": "CountryWithoutZip",
       |  "data": [
       |    {
       |      "activeFrom": "2023-01-23",
       |      "code": "GB",
       |      "state": "valid",
       |      "description": "United Kingdom"
       |    }
       |  ]
       |}
       |""".stripMargin

  private val unLocodeResponseJson: String =
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
      |{
      |  "data": [
      |    {
      |      "type": "A",
      |      "description": "Designated location"
      |    },
      |    {
      |      "type": "B",
      |      "description": "Authorised place"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val emptyResponseJson: String =
    """
      |{
      |  "data": []
      |}
      |""".stripMargin

  "Reference Data" - {

    "getTypeOfLocation" - {
      val url = s"/$baseUrl/lists/TypeOfLocation"
      "must return Seq of security types when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(locationTypesResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          LocationType("A", "Designated location"),
          LocationType("B", "Authorised place")
        )

        connector.getTypesOfLocation().futureValue mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty list of location types" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(emptyResponseJson))
        )

        whenReady[Throwable, Assertion](connector.getTypesOfLocation().failed) {
          _ mustBe a[NoReferenceDataFoundException]
        }
      }

      "should handle client and server errors for control types" in {
        checkErrorResponse(url, connector.getTypesOfLocation())
      }
    }

    "getCustomsOfficesForCountryAndRole" - {
      val role                   = "TRA"
      def url(countryId: String) = s"/$baseUrl/lists/CustomsOffices?data.countryId=$countryId&data.roles.role=TRA"

      "must return a successful future response with a sequence of CustomsOffices" in {
        val countryId = "GB"

        server.stubFor(
          get(urlEqualTo(url(countryId)))
            .willReturn(okJson(customsOfficesResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          CustomsOffice("GB1", "testName1", None, "GB"),
          CustomsOffice("GB2", "testName2", None, "GB")
        )

        connector.getCustomsOfficesForCountryAndRole(countryId, role).futureValue mustBe expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty list of customs offices" in {
        val countryId = "AR"

        server.stubFor(
          get(urlEqualTo(url(countryId)))
            .willReturn(okJson(emptyResponseJson))
        )

        whenReady[Throwable, Assertion](connector.getCustomsOfficesForCountryAndRole(countryId, role).failed) {
          _ mustBe a[NoReferenceDataFoundException]
        }
      }

      "must return an exception when an error response is returned" in {
        val countryId = "GB"
        checkErrorResponse(url(countryId), connector.getCustomsOfficesForCountryAndRole(countryId, role))
      }
    }

    "getCountries for full list" - {
      val url = s"/$baseUrl/lists/CountryCodesFullList"

      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countriesResponseJson("CountryCodesFullList")))
        )

        val expectedResult = NonEmptySet.of(
          Country(CountryCode("AD"), "Andorra"),
          Country(CountryCode("GB"), "United Kingdom")
        )

        connector.getCountries("CountryCodesFullList").futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getCountries("CountryCodesFullList"))
      }
    }

    "getCountriesWithoutZipCountry" - {
      def url(countryId: String) = s"/$baseUrl/lists/CountryWithoutZip?data.code=$countryId"

      "must return Seq of Country when successful" in {
        val countryId = "GB"
        server.stubFor(
          get(urlEqualTo(url(countryId)))
            .willReturn(okJson(countryWithoutZipResponseJson))
        )

        val expectedResult = CountryCode(countryId)

        connector.getCountriesWithoutZipCountry(countryId).futureValue mustEqual expectedResult
      }

      "must throw a NoReferenceDataFoundException for an empty response" in {
        val countryId = "FR"
        checkNoReferenceDataFoundResponse(url(countryId), connector.getCountriesWithoutZipCountry(countryId))
      }

      "must return an exception when an error response is returned" in {
        val countryId = "FR"
        checkErrorResponse(url(countryId), connector.getCountriesWithoutZipCountry(countryId))
      }
    }

    "getUnLocode" - {
      val code = "UN1"
      val url  = s"/$baseUrl/lists/UnLocodeExtended?data.unLocodeExtendedCode=UN1"

      "must return Seq of UN/LOCODES when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(unLocodeResponseJson))
        )

        val expectedResult = UnLocode("UN1", "testName1")

        connector.getUnLocode(code).futureValue mustEqual expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getUnLocode(code))
      }
    }

    "getSpecificCircumstanceIndicators" - {
      val url = s"/$baseUrl/lists/SpecificCircumstanceIndicatorCode"

      "must return Seq of specific circumstance indicators when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(specificCircumstanceIndicatorsResponseJson))
        )

        val expectedResult = NonEmptySet.of(
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

  private def checkNoReferenceDataFoundResponse(url: String, result: => Future[_]): Assertion = {
    server.stubFor(
      get(urlEqualTo(url))
        .willReturn(okJson(emptyResponseJson))
    )

    whenReady[Throwable, Assertion](result.failed) {
      _ mustBe a[NoReferenceDataFoundException]
    }
  }

  private def checkErrorResponse(url: String, result: => Future[_]): Assertion = {
    val errorResponses: Gen[Int] = Gen
      .chooseNum(400: Int, 599: Int)

    forAll(errorResponses) {
      errorResponse =>
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(errorResponse)
            )
        )

        whenReady[Throwable, Assertion](result.failed) {
          _ mustBe an[Exception]
        }
    }
  }

}
