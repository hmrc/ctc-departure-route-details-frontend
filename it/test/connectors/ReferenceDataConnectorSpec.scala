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
import play.api.test.Helpers.running

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

  private val emptyResponseJson: String =
    """
      |[]
      |""".stripMargin

  "Reference Data" - {

    "getTypesOfLocation" - {
      val url = s"/$baseUrl/lists/TypeOfLocation"


        val locationTypesResponseJson: String =
          """
            |[
            |  {
            |    "key": "A",
            |    "value": "Designated location"
            |  },
            |  {
            |    "key": "B",
            |    "value": "Authorised place"
            |  }
            |]
            |""".stripMargin

        "must return Seq of security types when successful" in {
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(locationTypesResponseJson))
              )

              val expectedResult = NonEmptySet.of(
                LocationType("A", "Designated location"),
                LocationType("B", "Authorised place")
              )

              connector.getTypesOfLocation().futureValue.value mustEqual expectedResult
        }

        "must return a NoReferenceDataFoundException for an empty response" in {
              checkNoReferenceDataFoundResponse(url, emptyResponseJson, connector.getTypesOfLocation())
        }

        "must return an exception when an error response is returned" in {
              checkErrorResponse(url, connector.getTypesOfLocation())
        }

    }

    "getCustomsOfficesForCountryAndRole" - {
      val role = "TRA"

        def url(countryId: String) = s"/$baseUrl/lists/CustomsOffices?countryCodes=$countryId&roles=$role"

        val customsOfficesResponseJson: String =
          """
            |[
            |  {
            |    "customsOfficeLsd": {
            |      "languageCode": "EN",
            |      "customsOfficeUsualName": "Glasgow Airport"
            |    },
            |    "referenceNumber": "GB000054",
            |    "countryCode": "GB"
            |  },
            |  {
            |    "customsOfficeLsd": {
            |      "languageCode": "EN",
            |      "customsOfficeUsualName": "Border Force, Port of Tyne"
            |    },
            |    "referenceNumber": "GB000218",
            |    "countryCode": "GB"
            |  }
            |]
            |""".stripMargin

        "must return a successful future response with a sequence of CustomsOffices" in {
          val countryId = "GB"

              server.stubFor(
                get(urlEqualTo(url(countryId)))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(customsOfficesResponseJson))
              )

              val expectedResult = NonEmptySet.of(
                CustomsOffice("GB000054", "Glasgow Airport", "GB"),
                CustomsOffice("GB000218", "Border Force, Port of Tyne", "GB")
              )

              connector.getCustomsOfficesForCountryAndRole(countryId, role).futureValue.value mustEqual expectedResult
          
        }

        "must return a NoReferenceDataFoundException for an empty response" in {
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              val countryId = "AR"
              checkNoReferenceDataFoundResponse(url(countryId), emptyResponseJson, connector.getCustomsOfficesForCountryAndRole(countryId, role))
          
        }

        "must return an exception when an error response is returned" in {
              val countryId = "GB"
              checkErrorResponse(url(countryId), connector.getCustomsOfficesForCountryAndRole(countryId, role))
        }

    }

    "getCountries" - {
      val listName = "CountryCodesFullList"
      val url      = s"/$baseUrl/lists/$listName"

        val countriesResponseJson: String =
          s"""
             |[
             |  {
             |    "key": "GB",
             |    "value": "United Kingdom"
             |  },
             |  {
             |    "key": "AD",
             |    "value": "Andorra"
             |  }
             |]
             |""".stripMargin

        "must return Seq of Country when successful" in {
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(countriesResponseJson))
              )

              val expectedResult = NonEmptySet.of(
                Country(CountryCode("AD"), "Andorra"),
                Country(CountryCode("GB"), "United Kingdom")
              )

              connector.getCountries(listName).futureValue.value mustEqual expectedResult
        }

        "must return a NoReferenceDataFoundException for an empty response" in {
              checkNoReferenceDataFoundResponse(url, emptyResponseJson, connector.getCountries(listName))
        }

        "must return an exception when an error response is returned" in {
              checkErrorResponse(url, connector.getCountries(listName))
        }

    }

    "getCountriesWithoutZipCountry" - {

        def url(countryId: String) = s"/$baseUrl/lists/CountryWithoutZip?keys=$countryId"

        val countryWithoutZipResponseJson: String =
          s"""
             |[
             |  {
             |    "key": "GB"
             |  }
             |]
             |""".stripMargin

        "must return Seq of Country when successful" in {
              val countryId = "GB"
              server.stubFor(
                get(urlEqualTo(url(countryId)))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(countryWithoutZipResponseJson))
              )

              val expectedResult = CountryCode(countryId)

              connector.getCountriesWithoutZipCountry(countryId).futureValue.value mustEqual expectedResult
        }

        "must return a NoReferenceDataFoundException for an empty response" in {
              val countryId = "FR"
              checkNoReferenceDataFoundResponse(url(countryId), emptyResponseJson, connector.getCountriesWithoutZipCountry(countryId))
        }

        "must return an exception when an error response is returned" in {
              val countryId = "FR"
              checkErrorResponse(url(countryId), connector.getCountriesWithoutZipCountry(countryId))
        }

    }

    "getUnLocode" - {
      val code = "UN1"

        val url = s"/$baseUrl/lists/UnLocodeExtended?keys=UN1"

        val unLocodeResponseJson: String =
          """
            |[
            |  {
            |    "key": "UN1",
            |    "value": "testName1"
            |  }
            |]
            |""".stripMargin

        "must return Seq of UN/LOCODES when successful" in {
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(unLocodeResponseJson))
              )

              val expectedResult = UnLocode("UN1", "testName1")

              connector.getUnLocode(code).futureValue.value mustEqual expectedResult
        }

        "must return a NoReferenceDataFoundException for an empty response" in {
              checkNoReferenceDataFoundResponse(url, emptyResponseJson, connector.getUnLocode(code))
        }

        "must return an exception when an error response is returned" in {
              checkErrorResponse(url, connector.getUnLocode(code))
        }

    }

    "getSpecificCircumstanceIndicators" - {
      val url = s"/$baseUrl/lists/SpecificCircumstanceIndicatorCode"

        val specificCircumstanceIndicatorsResponseJson: String =
          """
            |[
            |  {
            |    "key": "SCI1",
            |    "value": "testName1"
            |  },
            |  {
            |    "key": "SCI2",
            |    "value": "testName2"
            |  }
            |]
            |""".stripMargin

        "must return Seq of specific circumstance indicators when successful" in {
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(specificCircumstanceIndicatorsResponseJson))
              )

              val expectedResult = NonEmptySet.of(
                SpecificCircumstanceIndicator("SCI1", "testName1"),
                SpecificCircumstanceIndicator("SCI2", "testName2")
              )

              connector.getSpecificCircumstanceIndicators().futureValue.value mustEqual expectedResult
        }

        "must return a NoReferenceDataFoundException for an empty response" in {
              checkNoReferenceDataFoundResponse(url, emptyResponseJson, connector.getSpecificCircumstanceIndicators())
        }

        "must return an exception when an error response is returned" in {
              checkErrorResponse(url, connector.getSpecificCircumstanceIndicators())
        }
    }
  }

  private def checkNoReferenceDataFoundResponse(url: String, json: String, result: => Future[Either[Exception, ?]]): Assertion = {
    server.stubFor(
      get(urlEqualTo(url))
        .willReturn(okJson(json))
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
