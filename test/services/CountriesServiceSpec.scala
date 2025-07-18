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

package services

import base.SpecBase
import cats.data.NonEmptySet
import config.Constants.DeclarationType.TIR
import connectors.ReferenceDataConnector
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import generators.Generators
import models.reference.{Country, CountryCode}
import models.{Index, SelectableList}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.DeclarationTypePage
import pages.routing.index.{CountryOfRoutingInCL147Page, CountryOfRoutingPage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CountriesServiceSpec extends SpecBase with BeforeAndAfterEach with Generators with ScalaCheckPropertyChecks {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new CountriesService(mockRefDataConnector)

  private val country1: Country = Country(CountryCode("GB"), "United Kingdom")
  private val country2: Country = Country(CountryCode("FR"), "France")
  private val country3: Country = Country(CountryCode("ES"), "Spain")
  private val countries         = NonEmptySet.of(country1, country2, country3)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "CountriesService" - {

    "getDestinationCountries" - {

      "must call EU membership list (community countries) if TIR is selection" in {

        val userAnswers = emptyUserAnswers.setValue(DeclarationTypePage, TIR)

        when(mockRefDataConnector.getCountries(any())(any(), any()))
          .thenReturn(Future.successful(Right(countries)))

        service.getDestinationCountries(userAnswers).futureValue mustEqual
          SelectableList(Seq(country2, country3, country1))

        verify(mockRefDataConnector).getCountries(eqTo("CountryCodesCommunity"))(any(), any())
      }

      "must call CTC membership list (country common transit) if TIR is not selection" in {

        val generatedOption = arbitrary[String](arbitraryNonTIRDeclarationType).sample.value
        val userAnswers     = emptyUserAnswers.setValue(DeclarationTypePage, generatedOption)

        when(mockRefDataConnector.getCountries(any())(any(), any()))
          .thenReturn(Future.successful(Right(countries)))

        service.getDestinationCountries(userAnswers).futureValue mustEqual
          SelectableList(Seq(country2, country3, country1))

        verify(mockRefDataConnector).getCountries(eqTo("CountryCodesCommonTransit"))(any(), any())
      }
    }

    "getCountries" - {
      "must return a list of sorted countries" in {

        when(mockRefDataConnector.getCountries(any())(any(), any()))
          .thenReturn(Future.successful(Right(countries)))

        service.getCountries().futureValue mustEqual
          SelectableList(Seq(country2, country3, country1))

        verify(mockRefDataConnector).getCountries(eqTo("CountryCodesFullList"))(any(), any())
      }
    }

    "getCountriesOfRouting" - {
      "must return a list of sorted countries filtered  when 0 countries have been added" in {

        when(mockRefDataConnector.getCountries(any())(any(), any()))
          .thenReturn(Future.successful(Right(countries)))

        val userAnswers = emptyUserAnswers

        service.getCountriesOfRouting(userAnswers, index).futureValue mustEqual
          SelectableList(Seq(country2, country3, country1))

        verify(mockRefDataConnector).getCountries(eqTo("CountryCodesFullList"))(any(), any())
      }
      "must return a list of sorted countries filtered  when 1 country has been added" in {

        when(mockRefDataConnector.getCountries(any())(any(), any()))
          .thenReturn(Future.successful(Right(countries)))

        val userAnswers = emptyUserAnswers.setValue(CountryOfRoutingPage(index), country1)

        service.getCountriesOfRouting(userAnswers, index).futureValue mustEqual
          SelectableList(Seq(country2, country3, country1))

        verify(mockRefDataConnector).getCountries(eqTo("CountryCodesFullList"))(any(), any())
      }

      "must return a list of sorted countries filtered when 2 countries have been added" in {

        when(mockRefDataConnector.getCountries(any())(any(), any()))
          .thenReturn(Future.successful(Right(countries)))

        val userAnswers = emptyUserAnswers
          .setValue(CountryOfRoutingPage(index), country1)
          .setValue(CountryOfRoutingPage(Index(1)), country2)

        service.getCountriesOfRouting(userAnswers, Index(1)).futureValue mustEqual
          SelectableList(Seq(country2, country3))

        verify(mockRefDataConnector).getCountries(eqTo("CountryCodesFullList"))(any(), any())
      }

      "must return a list of sorted countries filtered when multiple have been added " in {

        when(mockRefDataConnector.getCountries(any())(any(), any()))
          .thenReturn(Future.successful(Right(countries)))

        val userAnswers = emptyUserAnswers
          .setValue(CountryOfRoutingPage(index), country2)
          .setValue(CountryOfRoutingPage(Index(1)), country3)
          .setValue(CountryOfRoutingPage(Index(2)), country1)
        countries.toSeq.zipWithIndex.map {
          case (country, i) =>
            service.getCountriesOfRouting(userAnswers, Index(i)).futureValue mustEqual
              SelectableList(Seq(country))
        }
        verify(mockRefDataConnector, times(countries.toSeq.size)).getCountries(eqTo("CountryCodesFullList"))(any(), any())
      }
    }

    "isInCL112" - {
      "must return true" - {
        "when connector call returns the country" in {
          forAll(nonEmptyString, arbitrary[Country]) {
            (countryId, country) =>
              beforeEach()

              when(mockRefDataConnector.getCountry(any(), any())(any(), any()))
                .thenReturn(Future.successful(Right(country)))

              val result = service.isInCL112(countryId).futureValue

              result mustEqual true

              verify(mockRefDataConnector).getCountry(eqTo("CountryCodesCTC"), eqTo(countryId))(any(), any())
          }
        }
      }

      "must return false" - {
        "when connector call returns NoReferenceDataFoundException" in {
          forAll(nonEmptyString) {
            countryId =>
              when(mockRefDataConnector.getCountry(any(), any())(any(), any()))
                .thenReturn(Future.successful(Left(NoReferenceDataFoundException(""))))

              val result = service.isInCL112(countryId).futureValue

              result mustEqual false
          }
        }
      }

      "must fail" - {
        "when connector call otherwise fails" in {
          forAll(nonEmptyString) {
            countryId =>
              when(mockRefDataConnector.getCountry(any(), any())(any(), any()))
                .thenReturn(Future.failed(new Throwable("")))

              val result = service.isInCL112(countryId)

              result.failed.futureValue mustBe a[Throwable]
          }
        }
      }
    }

    "isInCL147" - {
      "must return true" - {
        "when connector call returns the country" in {
          forAll(nonEmptyString, arbitrary[Country]) {
            (countryId, country) =>
              beforeEach()

              when(mockRefDataConnector.getCountry(any(), any())(any(), any()))
                .thenReturn(Future.successful(Right(country)))

              val result = service.isInCL147(countryId).futureValue

              result mustEqual true

              verify(mockRefDataConnector).getCountry(eqTo("CountryCustomsSecurityAgreementArea"), eqTo(countryId))(any(), any())
          }
        }
      }

      "must return false" - {
        "when connector call returns NoReferenceDataFoundException" in {
          forAll(nonEmptyString) {
            countryId =>
              when(mockRefDataConnector.getCountry(any(), any())(any(), any()))
                .thenReturn(Future.successful(Left(NoReferenceDataFoundException(""))))

              val result = service.isInCL147(countryId).futureValue

              result mustEqual false
          }
        }
      }

      "must fail" - {
        "when connector call otherwise fails" in {
          forAll(nonEmptyString) {
            countryId =>
              when(mockRefDataConnector.getCountry(any(), any())(any(), any()))
                .thenReturn(Future.failed(new Throwable("")))

              val result = service.isInCL147(countryId)

              result.failed.futureValue mustBe a[Throwable]
          }
        }
      }
    }

    "isInCL010" - {
      "must return true" - {
        "when connector call returns the country" in {
          forAll(nonEmptyString, arbitrary[Country]) {
            (countryId, country) =>
              beforeEach()

              when(mockRefDataConnector.getCountry(any(), any())(any(), any()))
                .thenReturn(Future.successful(Right(country)))

              val result = service.isInCL010(countryId).futureValue

              result mustEqual true

              verify(mockRefDataConnector).getCountry(eqTo("CountryCodesCommunity"), eqTo(countryId))(any(), any())
          }
        }
      }

      "must return false" - {
        "when connector call returns NoReferenceDataFoundException" in {
          forAll(nonEmptyString) {
            countryId =>
              when(mockRefDataConnector.getCountry(any(), any())(any(), any()))
                .thenReturn(Future.successful(Left(NoReferenceDataFoundException(""))))

              val result = service.isInCL010(countryId).futureValue

              result mustEqual false
          }
        }
      }

      "must fail" - {
        "when connector call otherwise fails" in {
          forAll(nonEmptyString) {
            countryId =>
              when(mockRefDataConnector.getCountry(any(), any())(any(), any()))
                .thenReturn(Future.failed(new Throwable("")))

              val result = service.isInCL010(countryId)

              result.failed.futureValue mustBe a[Throwable]
          }
        }
      }
    }

    "getAddressPostcodeBasedCountries" - {
      "must return a list of sorted address postcode based countries" in {

        when(mockRefDataConnector.getCountries(any())(any(), any()))
          .thenReturn(Future.successful(Right(countries)))

        service.getAddressPostcodeBasedCountries().futureValue mustEqual
          SelectableList(Seq(country2, country3, country1))

        verify(mockRefDataConnector).getCountries(eqTo("CountryAddressPostcodeBased"))(any(), any())
      }
    }

    "getOfficeOfTransitCountries" - {
      "when there are one or more countries of routing in user answers" - {
        "must return countries of routing" in {
          forAll(Gen.choose(1, frontendAppConfig.maxCountriesOfRouting)) {
            numberOfCountries =>
              val userAnswers = (0 until numberOfCountries).foldLeft(emptyUserAnswers) {
                (acc, i) =>
                  acc.setValue(CountryOfRoutingPage(Index(i)), country1)
              }
              val result = service.getOfficeOfTransitCountries(userAnswers).futureValue

              result.values mustEqual Seq.fill(numberOfCountries)(country1)

              verify(mockRefDataConnector, never()).getCountries(any())(any(), any())
          }
        }
      }

      "when there are no countries of routing in user answers" - {
        "must call getCountries" in {
          when(mockRefDataConnector.getCountries(any())(any(), any()))
            .thenReturn(Future.successful(Right(countries)))

          val result = service.getOfficeOfTransitCountries(emptyUserAnswers).futureValue

          result.values mustEqual Seq(country2, country3, country1)

          verify(mockRefDataConnector).getCountries(any())(any(), any())
        }
      }
    }

    "getOfficeOfExitCountries" - {
      "when there are one or more countries of routing in user answers" - {
        "must return countries that are in the countries of routing, in the CL147 and aren't the country of destination" in {
          val country4: Country = Country(CountryCode("DE"), "Germany")

          val userAnswers = emptyUserAnswers
            .setValue(CountryOfRoutingPage(Index(0)), country1)
            .setValue(CountryOfRoutingInCL147Page(Index(0)), true)
            .setValue(CountryOfRoutingPage(Index(1)), country2)
            .setValue(CountryOfRoutingInCL147Page(Index(1)), true)
            .setValue(CountryOfRoutingPage(Index(2)), country3)
            .setValue(CountryOfRoutingInCL147Page(Index(2)), true)
            .setValue(CountryOfRoutingPage(Index(3)), country4)
            .setValue(CountryOfRoutingInCL147Page(Index(3)), false)

          val result = service.getOfficeOfExitCountries(userAnswers, country3).futureValue

          result.values mustEqual Seq(country1, country2)

          verify(mockRefDataConnector, never()).getCountries(eqTo("CountryCodesFullList"))(any(), any())
        }
      }

      "when there are no countries of routing in user answers" - {
        "must call getCountries" in {
          forAll(arbitrary[Country]) {
            countryOfDestination =>
              beforeEach()

              when(mockRefDataConnector.getCountries(any())(any(), any()))
                .thenReturn(Future.successful(Right(countries)))

              val result = service.getOfficeOfExitCountries(emptyUserAnswers, countryOfDestination).futureValue

              result.values mustEqual Seq(country2, country3, country1)

              verify(mockRefDataConnector).getCountries(eqTo("CountryCodesFullList"))(any(), any())
          }
        }
      }
    }

    "doesCountryRequireZip" - {
      "must return true" - {
        "when countries without zip doesn't contain this country" in {
          forAll(arbitrary[Country]) {
            country =>
              when(mockRefDataConnector.getCountriesWithoutZipCountry(any())(any(), any()))
                .thenReturn(Future.successful(Right(country.code)))

              val result = service.doesCountryRequireZip(country).futureValue

              result mustEqual true
          }
        }
      }

      "must return false" - {
        "when countries without zip does contain this country" in {
          forAll(arbitrary[Country]) {
            country =>
              when(mockRefDataConnector.getCountriesWithoutZipCountry(any())(any(), any()))
                .thenReturn(Future.successful(Left(NoReferenceDataFoundException(""))))

              val result = service.doesCountryRequireZip(country).futureValue

              result mustEqual false
          }
        }
      }
    }
  }
}
