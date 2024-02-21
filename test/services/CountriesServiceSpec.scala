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
import generators.Generators
import models.reference.{Country, CountryCode}
import models.{Index, SelectableList}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.DeclarationTypePage
import pages.routing.index.CountryOfRoutingPage

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
          .thenReturn(Future.successful(countries))

        service.getDestinationCountries(userAnswers).futureValue mustBe
          SelectableList(Seq(country2, country3, country1))

        verify(mockRefDataConnector).getCountries(eqTo("CountryCodesCommunity"))(any(), any())
      }

      "must call CTC membership list (country common transit) if TIR is not selection" in {

        val generatedOption = arbitrary[String](arbitraryNonTIRDeclarationType).sample.value
        val userAnswers     = emptyUserAnswers.setValue(DeclarationTypePage, generatedOption)

        when(mockRefDataConnector.getCountries(any())(any(), any()))
          .thenReturn(Future.successful(countries))

        service.getDestinationCountries(userAnswers).futureValue mustBe
          SelectableList(Seq(country2, country3, country1))

        verify(mockRefDataConnector).getCountries(eqTo("CountryCodesCommonTransit"))(any(), any())
      }
    }

    "getCountries" - {
      "must return a list of sorted countries" in {

        when(mockRefDataConnector.getCountries(any())(any(), any()))
          .thenReturn(Future.successful(countries))

        service.getCountries().futureValue mustBe
          SelectableList(Seq(country2, country3, country1))

        verify(mockRefDataConnector).getCountries(eqTo("CountryCodesFullList"))(any(), any())
      }
    }

    "getTransitCountries" - {
      "must return a list of sorted transit countries" in {

        when(mockRefDataConnector.getCountries(any())(any(), any()))
          .thenReturn(Future.successful(countries))

        service.getTransitCountries().futureValue mustBe
          SelectableList(Seq(country2, country3, country1))

        verify(mockRefDataConnector).getCountries(eqTo("CountryCodesCommonTransit"))(any(), any())
      }
    }

    "getCommunityCountries" - {
      "must return a list of sorted EU transit countries" in {

        when(mockRefDataConnector.getCountries(any())(any(), any()))
          .thenReturn(Future.successful(countries))

        service.getCommunityCountries().futureValue mustBe
          SelectableList(Seq(country2, country3, country1))

        verify(mockRefDataConnector).getCountries(eqTo("CountryCodesCommunity"))(any(), any())
      }
    }

    "getCustomsSecurityAgreementAreaCountries" - {
      "must return a list of sorted customs security agreement area countries" in {

        when(mockRefDataConnector.getCountries(any())(any(), any()))
          .thenReturn(Future.successful(countries))

        service.getCustomsSecurityAgreementAreaCountries().futureValue mustBe
          SelectableList(Seq(country2, country3, country1))

        verify(mockRefDataConnector).getCountries(eqTo("CountryCustomsSecurityAgreementArea"))(any(), any())
      }
    }

    "getCountryCodesCTC" - {
      "must return a list of sorted customs security agreement area countries" in {

        when(mockRefDataConnector.getCountries(any())(any(), any()))
          .thenReturn(Future.successful(countries))

        service.getCountryCodesCTC().futureValue mustBe
          SelectableList(Seq(country2, country3, country1))

        verify(mockRefDataConnector).getCountries(eqTo("CountryCodesCTC"))(any(), any())
      }
    }

    "getCountriesWithoutZip" - {
      "must return a list of countries without ZIP codes" in {

        when(mockRefDataConnector.getCountriesWithoutZip()(any(), any()))
          .thenReturn(Future.successful(countries.map(_.code)))

        service.getCountriesWithoutZip().futureValue mustBe
          Seq(country3.code, country2.code, country1.code)

        verify(mockRefDataConnector).getCountriesWithoutZip()(any(), any())
      }
    }

    "getAddressPostcodeBasedCountries" - {
      "must return a list of sorted address postcode based countries" in {

        when(mockRefDataConnector.getCountries(any())(any(), any()))
          .thenReturn(Future.successful(countries))

        service.getAddressPostcodeBasedCountries().futureValue mustBe
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

              result.values mustBe Seq.fill(numberOfCountries)(country1)

              verify(mockRefDataConnector, never()).getCountries(any())(any(), any())
          }
        }
      }

      "when there are no countries of routing in user answers" - {
        "must call getCountries" in {
          when(mockRefDataConnector.getCountries(any())(any(), any()))
            .thenReturn(Future.successful(countries))

          val result = service.getOfficeOfTransitCountries(emptyUserAnswers).futureValue

          result.values mustBe Seq(country2, country3, country1)

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
            .setValue(CountryOfRoutingPage(Index(1)), country2)
            .setValue(CountryOfRoutingPage(Index(2)), country3)
            .setValue(CountryOfRoutingPage(Index(3)), country4)

          when(mockRefDataConnector.getCountries(any())(any(), any()))
            .thenReturn(Future.successful(countries))

          val result = service.getOfficeOfExitCountries(userAnswers, country3).futureValue

          result.values mustBe Seq(country1, country2)

          verify(mockRefDataConnector).getCountries(eqTo("CountryCustomsSecurityAgreementArea"))(any(), any())
          verify(mockRefDataConnector, never()).getCountries(eqTo("CountryCodesFullList"))(any(), any())
        }
      }

      "when there are no countries of routing in user answers" - {
        "must call getCountries" in {
          forAll(arbitrary[Country]) {
            countryOfDestination =>
              beforeEach()

              when(mockRefDataConnector.getCountries(any())(any(), any()))
                .thenReturn(Future.successful(countries))

              val result = service.getOfficeOfExitCountries(emptyUserAnswers, countryOfDestination).futureValue

              result.values mustBe Seq(country2, country3, country1)

              verify(mockRefDataConnector, never()).getCountries(eqTo("CountryCustomsSecurityAgreementArea"))(any(), any())
              verify(mockRefDataConnector).getCountries(eqTo("CountryCodesFullList"))(any(), any())
          }
        }
      }
    }

    "doesCountryRequireZip" - {
      "must return true" - {
        "when countries without zip doesn't contain this country" in {
          when(mockRefDataConnector.getCountriesWithoutZip()(any(), any()))
            .thenReturn(Future.successful(countries.map(_.code)))

          val country = Arbitrary.arbitrary[Country].retryUntil(!countries.contains(_)).sample.value

          val result = service.doesCountryRequireZip(country).futureValue

          result mustBe true

        }
      }

      "must return false" - {
        "when countries without zip does contain this country" in {
          when(mockRefDataConnector.getCountriesWithoutZip()(any(), any()))
            .thenReturn(Future.successful(countries.map(_.code)))

          val country = countries.head

          val result = service.doesCountryRequireZip(country).futureValue

          result mustBe false

        }
      }
    }
  }
}
