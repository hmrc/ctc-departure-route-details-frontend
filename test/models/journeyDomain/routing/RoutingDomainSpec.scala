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

package models.journeyDomain.routing

import base.SpecBase
import config.Constants.SecurityType.*
import generators.Generators
import models.Index
import models.journeyDomain.UserAnswersReader
import models.reference.{Country, CustomsOffice}
import org.scalacheck.Arbitrary.arbitrary
import pages.external.SecurityDetailsTypePage
import pages.routing.*
import pages.routing.index.CountryOfRoutingPage
import pages.sections.routing.{CountryOfRoutingSection, RoutingSection}

class RoutingDomainSpec extends SpecBase with Generators {

  "RoutingDomain" - {

    val officeOfDestination = arbitrary[CustomsOffice].sample.value
    val country             = arbitrary[Country].sample.value

    "can be parsed from UserAnswers" - {
      "when no security" - {

        val securityType = NoSecurityDetails

        "and following binding itinerary" in {

          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, securityType)
            .setValue(CountryOfDestinationPage, country)
            .setValue(OfficeOfDestinationPage, officeOfDestination)
            .setValue(BindingItineraryPage, true)
            .setValue(CountryOfRoutingPage(index), country)

          val expectedResult = RoutingDomain(
            countryOfDestination = country,
            officeOfDestination = officeOfDestination,
            bindingItinerary = true,
            countriesOfRouting = CountriesOfRoutingDomain(
              Seq(
                CountryOfRoutingDomain(country)(index)
              )
            )
          )

          val result = UserAnswersReader[RoutingDomain](
            RoutingDomain.userAnswersReader.apply(Nil)
          ).run(userAnswers)

          result.value.value mustEqual expectedResult
          result.value.pages mustEqual Seq(
            CountryOfDestinationPage,
            OfficeOfDestinationPage,
            BindingItineraryPage,
            CountryOfRoutingPage(index),
            CountryOfRoutingSection(index),
            AddAnotherCountryOfRoutingPage,
            RoutingSection
          )
        }

        "and not following binding itinerary" in {

          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, securityType)
            .setValue(CountryOfDestinationPage, country)
            .setValue(OfficeOfDestinationPage, officeOfDestination)
            .setValue(BindingItineraryPage, false)
            .setValue(AddCountryOfRoutingYesNoPage, false)

          val expectedResult = RoutingDomain(
            countryOfDestination = country,
            officeOfDestination = officeOfDestination,
            bindingItinerary = false,
            countriesOfRouting = CountriesOfRoutingDomain(Nil)
          )

          val result = UserAnswersReader[RoutingDomain](
            RoutingDomain.userAnswersReader.apply(Nil)
          ).run(userAnswers)

          result.value.value mustEqual expectedResult
          result.value.pages mustEqual Seq(
            CountryOfDestinationPage,
            OfficeOfDestinationPage,
            BindingItineraryPage,
            AddCountryOfRoutingYesNoPage,
            RoutingSection
          )
        }
      }

      "when there is security" - {

        val securityType = arbitrary[String](arbitrarySomeSecurityDetailsType).sample.value

        "and following binding itinerary" in {

          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, securityType)
            .setValue(CountryOfDestinationPage, country)
            .setValue(OfficeOfDestinationPage, officeOfDestination)
            .setValue(BindingItineraryPage, true)
            .setValue(CountryOfRoutingPage(index), country)

          val expectedResult = RoutingDomain(
            countryOfDestination = country,
            officeOfDestination = officeOfDestination,
            bindingItinerary = true,
            countriesOfRouting = CountriesOfRoutingDomain(
              Seq(
                CountryOfRoutingDomain(country)(index)
              )
            )
          )

          val result = UserAnswersReader[RoutingDomain](
            RoutingDomain.userAnswersReader.apply(Nil)
          ).run(userAnswers)

          result.value.value mustEqual expectedResult
          result.value.pages mustEqual Seq(
            CountryOfDestinationPage,
            OfficeOfDestinationPage,
            BindingItineraryPage,
            CountryOfRoutingPage(index),
            CountryOfRoutingSection(index),
            AddAnotherCountryOfRoutingPage,
            RoutingSection
          )
        }

        "and not following binding itinerary" in {

          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, securityType)
            .setValue(CountryOfDestinationPage, country)
            .setValue(OfficeOfDestinationPage, officeOfDestination)
            .setValue(BindingItineraryPage, false)
            .setValue(CountryOfRoutingPage(index), country)

          val expectedResult = RoutingDomain(
            countryOfDestination = country,
            officeOfDestination = officeOfDestination,
            bindingItinerary = false,
            countriesOfRouting = CountriesOfRoutingDomain(
              Seq(
                CountryOfRoutingDomain(country)(index)
              )
            )
          )

          val result = UserAnswersReader[RoutingDomain](
            RoutingDomain.userAnswersReader.apply(Nil)
          ).run(userAnswers)

          result.value.value mustEqual expectedResult
          result.value.pages mustEqual Seq(
            CountryOfDestinationPage,
            OfficeOfDestinationPage,
            BindingItineraryPage,
            CountryOfRoutingPage(index),
            CountryOfRoutingSection(index),
            AddAnotherCountryOfRoutingPage,
            RoutingSection
          )
        }
      }
    }

    "cannot be parsed from UserAnswers" - {

      "when country of destination page is missing" in {

        val securityType = arbitrary[String](arbitrarySecurityDetailsType).sample.value
        val userAnswers  = emptyUserAnswers.setValue(SecurityDetailsTypePage, securityType)

        val result = UserAnswersReader[RoutingDomain](
          RoutingDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.left.value.page mustEqual CountryOfDestinationPage
        result.left.value.pages mustEqual Seq(
          CountryOfDestinationPage
        )
      }

      "when office of destination page is missing" in {

        val securityType = arbitrary[String](arbitrarySecurityDetailsType).sample.value
        val userAnswers = emptyUserAnswers
          .setValue(SecurityDetailsTypePage, securityType)
          .setValue(CountryOfDestinationPage, country)

        val result = UserAnswersReader[RoutingDomain](
          RoutingDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.left.value.page mustEqual OfficeOfDestinationPage
        result.left.value.pages mustEqual Seq(
          CountryOfDestinationPage,
          OfficeOfDestinationPage
        )
      }

      "when binding itinerary page is missing" in {

        val securityType = arbitrary[String](arbitrarySecurityDetailsType).sample.value
        val userAnswers = emptyUserAnswers
          .setValue(SecurityDetailsTypePage, securityType)
          .setValue(CountryOfDestinationPage, country)
          .setValue(OfficeOfDestinationPage, officeOfDestination)

        val result = UserAnswersReader[RoutingDomain](
          RoutingDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.left.value.page mustEqual BindingItineraryPage
        result.left.value.pages mustEqual Seq(
          CountryOfDestinationPage,
          OfficeOfDestinationPage,
          BindingItineraryPage
        )
      }

      "when add country page is missing" in {

        val userAnswers = emptyUserAnswers
          .setValue(SecurityDetailsTypePage, NoSecurityDetails)
          .setValue(CountryOfDestinationPage, country)
          .setValue(OfficeOfDestinationPage, officeOfDestination)
          .setValue(BindingItineraryPage, false)

        val result = UserAnswersReader[RoutingDomain](
          RoutingDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.left.value.page mustEqual AddCountryOfRoutingYesNoPage
        result.left.value.pages mustEqual Seq(
          CountryOfDestinationPage,
          OfficeOfDestinationPage,
          BindingItineraryPage,
          AddCountryOfRoutingYesNoPage
        )
      }

      "when binding itinerary is true and no countries added" in {

        val securityType = arbitrary[String](arbitrarySecurityDetailsType).sample.value
        val userAnswers = emptyUserAnswers
          .setValue(SecurityDetailsTypePage, securityType)
          .setValue(CountryOfDestinationPage, country)
          .setValue(OfficeOfDestinationPage, officeOfDestination)
          .setValue(BindingItineraryPage, true)

        val result = UserAnswersReader[RoutingDomain](
          RoutingDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.left.value.page mustEqual CountryOfRoutingPage(Index(0))
        result.left.value.pages mustEqual Seq(
          CountryOfDestinationPage,
          OfficeOfDestinationPage,
          BindingItineraryPage,
          CountryOfRoutingPage(Index(0))
        )
      }

      "when add country is true and no countries added" in {

        val userAnswers = emptyUserAnswers
          .setValue(SecurityDetailsTypePage, NoSecurityDetails)
          .setValue(CountryOfDestinationPage, country)
          .setValue(OfficeOfDestinationPage, officeOfDestination)
          .setValue(BindingItineraryPage, false)
          .setValue(AddCountryOfRoutingYesNoPage, true)

        val result = UserAnswersReader[RoutingDomain](
          RoutingDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.left.value.page mustEqual CountryOfRoutingPage(Index(0))
        result.left.value.pages mustEqual Seq(
          CountryOfDestinationPage,
          OfficeOfDestinationPage,
          BindingItineraryPage,
          AddCountryOfRoutingYesNoPage,
          CountryOfRoutingPage(Index(0))
        )
      }

      "when there's security and no countries added" in {

        val securityType     = arbitrary[String](arbitrarySomeSecurityDetailsType).sample.value
        val bindingItinerary = arbitrary[Boolean].sample.value
        val userAnswers = emptyUserAnswers
          .setValue(SecurityDetailsTypePage, securityType)
          .setValue(CountryOfDestinationPage, country)
          .setValue(OfficeOfDestinationPage, officeOfDestination)
          .setValue(BindingItineraryPage, bindingItinerary)

        val result = UserAnswersReader[RoutingDomain](
          RoutingDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.left.value.page mustEqual CountryOfRoutingPage(Index(0))
        result.left.value.pages mustEqual Seq(
          CountryOfDestinationPage,
          OfficeOfDestinationPage,
          BindingItineraryPage,
          CountryOfRoutingPage(Index(0))
        )
      }
    }
  }
}
