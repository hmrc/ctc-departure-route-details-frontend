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
import com.github.tomakehurst.wiremock.client.WireMock.*
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import itbase.{ItSpecBase, WireMockServerHandler}
import models.reference.*
import org.scalacheck.Gen
import org.scalatest.{Assertion, EitherValues}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec extends ItSpecBase with WireMockServerHandler with ScalaCheckPropertyChecks with EitherValues {

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
      |      "languageCode": "EN",
      |      "name": "CUSTOMS OFFICE SANT JULIÀ DE LÒRIA",
      |      "phoneNumber": "+ (376) 84 1090",
      |      "id": "AD000001",
      |      "countryId": "AD",
      |      "roles": [
      |        {
      |          "role": "AUT"
      |        },
      |        {
      |          "role": "DEP"
      |        },
      |        {
      |          "role": "DES"
      |        },
      |        {
      |          "role": "TRA"
      |        }
      |      ]
      |    },
      |    {
      |      "languageCode": "ES",
      |      "name": "ADUANA DE ST. JULIÀ DE LÒRIA",
      |      "phoneNumber": "+ (376) 84 1090",
      |      "id": "AD000001",
      |      "countryId": "AD",
      |      "roles": [
      |        {
      |          "role": "AUT"
      |        },
      |        {
      |          "role": "DEP"
      |        },
      |        {
      |          "role": "DES"
      |        },
      |        {
      |          "role": "TRA"
      |        }
      |      ]
      |    },
      |    {
      |      "languageCode": "FR",
      |      "name": "BUREAU DE SANT JULIÀ DE LÒRIA",
      |      "phoneNumber": "+ (376) 84 1090",
      |      "id": "AD000001",
      |      "countryId": "AD",
      |      "roles": [
      |        {
      |          "role": "AUT"
      |        },
      |        {
      |          "role": "DEP"
      |        },
      |        {
      |          "role": "DES"
      |        },
      |        {
      |          "role": "TRA"
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

        connector.getTypesOfLocation().futureValue.value mustEqual expectedResult
      }

      "must return a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getTypesOfLocation())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getTypesOfLocation())
      }
    }

    "getCustomsOfficesForCountryAndRole" - {
      val role                   = "TRA"
      def url(countryId: String) = s"/$baseUrl/lists/CustomsOffices?data.countryId=$countryId&data.roles.role=TRA"

      "must return a successful future response with a sequence of CustomsOffices" in {
        val countryId = "AD"

        server.stubFor(
          get(urlEqualTo(url(countryId)))
            .willReturn(okJson(customsOfficesResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          CustomsOffice("AD000001", "CUSTOMS OFFICE SANT JULIÀ DE LÒRIA", "AD")
        )

        connector.getCustomsOfficesForCountryAndRole(countryId, role).futureValue.value mustBe expectedResult
      }

      "must return a NoReferenceDataFoundException for an empty response" in {
        val countryId = "AR"
        checkNoReferenceDataFoundResponse(url(countryId), connector.getCustomsOfficesForCountryAndRole(countryId, role))
      }

      "must return an exception when an error response is returned" in {
        val countryId = "GB"
        checkErrorResponse(url(countryId), connector.getCustomsOfficesForCountryAndRole(countryId, role))
      }
    }

    "getCountries" - {
      val listName = "CountryCodesFullList"
      val url      = s"/$baseUrl/lists/$listName"

      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countriesResponseJson(listName)))
        )

        val expectedResult = NonEmptySet.of(
          Country(CountryCode("AD"), "Andorra"),
          Country(CountryCode("GB"), "United Kingdom")
        )

        connector.getCountries(listName).futureValue.value mustEqual expectedResult
      }

      "must return a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCountries(listName))
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getCountries(listName))
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

        connector.getCountriesWithoutZipCountry(countryId).futureValue.value mustEqual expectedResult
      }

      "must return a NoReferenceDataFoundException for an empty response" in {
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

        connector.getUnLocode(code).futureValue.value mustEqual expectedResult
      }

      "must return a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getUnLocode(code))
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

        connector.getSpecificCircumstanceIndicators().futureValue.value mustEqual expectedResult
      }

      "must return a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getSpecificCircumstanceIndicators())
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(url, connector.getSpecificCircumstanceIndicators())
      }
    }
  }

  private def checkNoReferenceDataFoundResponse(url: String, result: => Future[Either[Exception, ?]]): Assertion = {
    server.stubFor(
      get(urlEqualTo(url))
        .willReturn(okJson(emptyResponseJson))
    )

    result.futureValue.left.value mustBe a[NoReferenceDataFoundException]
  }

  private def checkErrorResponse(url: String, result: => Future[Either[Exception, ?]]): Assertion = {
    val errorResponses: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)

    forAll(errorResponses) {
      errorResponse =>
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(errorResponse)
            )
        )

        result.futureValue.left.value mustBe an[Exception]
    }
  }
}
