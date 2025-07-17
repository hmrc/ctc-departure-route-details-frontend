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

package models.journeyDomain.transit

import base.SpecBase
import config.Constants.CountryCode.*
import config.Constants.SecurityType.*
import generators.Generators
import models.journeyDomain.UserAnswersReader
import models.reference.{Country, CustomsOffice}
import models.{DateTime, Index}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.external.SecurityDetailsTypePage
import pages.routing.*
import pages.sections.transit.OfficeOfTransitSection
import pages.transit.index.*

class OfficeOfTransitDomainSpec extends SpecBase with Generators {

  "OfficeOfTransitDomain" - {

    val customsOffice = arbitrary[CustomsOffice].sample.value

    val country = arbitrary[Country].sample.value

    val eta = arbitrary[DateTime].sample.value

    val securityType1Or3    = Gen.oneOf(EntrySummaryDeclarationSecurityDetails, EntryAndExitSummaryDeclarationSecurityDetails).sample.value
    val securityTypeNot1Or3 = Gen.oneOf(NoSecurityDetails, ExitSummaryDeclarationSecurityDetails).sample.value

    val officeOfTransit = arbitrary[CustomsOffice].sample.value

    "can be parsed from UserAnswers" - {
      "when first in sequence" - {
        val index = Index(0)

        "and office of destination is in set CL112" in {
          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, NoSecurityDetails)
            .setValue(OfficeOfDestinationInCL112Page, true)
            .setValue(OfficeOfTransitPage(index), officeOfTransit)
            .setValue(AddOfficeOfTransitETAYesNoPage(index), false)

          val expectedResult = OfficeOfTransitDomain(
            country = None,
            customsOffice = officeOfTransit,
            eta = None
          )(index)

          val result = UserAnswersReader[OfficeOfTransitDomain](
            OfficeOfTransitDomain.userAnswersReader(index).apply(Nil)
          ).run(userAnswers)

          result.value.value mustEqual expectedResult
          result.value.pages mustEqual Seq(
            OfficeOfTransitPage(index),
            AddOfficeOfTransitETAYesNoPage(index),
            OfficeOfTransitSection(index)
          )
        }

        "and office of destination is in 'AD'" in {
          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, NoSecurityDetails)
            .setValue(OfficeOfDestinationPage, customsOffice.copy(countryId = AD))
            .setValue(OfficeOfDestinationInCL112Page, false)
            .setValue(OfficeOfTransitPage(index), officeOfTransit)
            .setValue(AddOfficeOfTransitETAYesNoPage(index), false)

          val expectedResult = OfficeOfTransitDomain(
            country = None,
            customsOffice = officeOfTransit,
            eta = None
          )(index)

          val result = UserAnswersReader[OfficeOfTransitDomain](
            OfficeOfTransitDomain.userAnswersReader(index).apply(Nil)
          ).run(userAnswers)

          result.value.value mustEqual expectedResult
          result.value.pages mustEqual Seq(
            OfficeOfDestinationPage,
            OfficeOfTransitPage(index),
            AddOfficeOfTransitETAYesNoPage(index),
            OfficeOfTransitSection(index)
          )
        }

        "and office of destination is not in 'AD'" in {
          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, NoSecurityDetails)
            .setValue(OfficeOfDestinationPage, customsOffice)
            .setValue(OfficeOfDestinationInCL112Page, false)
            .setValue(OfficeOfTransitCountryPage(index), country)
            .setValue(OfficeOfTransitPage(index), officeOfTransit)
            .setValue(AddOfficeOfTransitETAYesNoPage(index), false)

          val expectedResult = OfficeOfTransitDomain(
            country = Some(country),
            customsOffice = officeOfTransit,
            eta = None
          )(index)

          val result = UserAnswersReader[OfficeOfTransitDomain](
            OfficeOfTransitDomain.userAnswersReader(index).apply(Nil)
          ).run(userAnswers)

          result.value.value mustEqual expectedResult
          result.value.pages mustEqual Seq(
            OfficeOfDestinationPage,
            OfficeOfTransitCountryPage(index),
            OfficeOfTransitPage(index),
            AddOfficeOfTransitETAYesNoPage(index),
            OfficeOfTransitSection(index)
          )
        }
      }

      "when not first in sequence" in {
        val index = Index(1)

        val userAnswers = emptyUserAnswers
          .setValue(SecurityDetailsTypePage, NoSecurityDetails)
          .setValue(OfficeOfDestinationInCL112Page, false)
          .setValue(OfficeOfTransitCountryPage(Index(0)), country)
          .setValue(OfficeOfTransitCountryPage(index), country)
          .setValue(OfficeOfTransitPage(index), officeOfTransit)
          .setValue(AddOfficeOfTransitETAYesNoPage(index), true)
          .setValue(OfficeOfTransitETAPage(index), eta)

        val expectedResult = OfficeOfTransitDomain(
          country = Some(country),
          customsOffice = officeOfTransit,
          eta = Some(eta)
        )(index)

        val result = UserAnswersReader[OfficeOfTransitDomain](
          OfficeOfTransitDomain.userAnswersReader(index).apply(Nil)
        ).run(userAnswers)

        result.value.value mustEqual expectedResult
        result.value.pages mustEqual Seq(
          OfficeOfTransitCountryPage(index),
          OfficeOfTransitPage(index),
          AddOfficeOfTransitETAYesNoPage(index),
          OfficeOfTransitETAPage(index),
          OfficeOfTransitSection(index)
        )
      }

      "when security type is one of 'entrySummaryDeclaration' or 'entryAndExitSummaryDeclaration'" - {
        "and office of transit is in set CL147" in {
          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, securityType1Or3)
            .setValue(OfficeOfDestinationPage, customsOffice)
            .setValue(OfficeOfDestinationInCL112Page, false)
            .setValue(OfficeOfTransitCountryPage(index), country)
            .setValue(OfficeOfTransitPage(index), officeOfTransit)
            .setValue(OfficeOfTransitInCL147Page(index), true)
            .setValue(OfficeOfTransitETAPage(index), eta)

          val expectedResult = OfficeOfTransitDomain(
            country = Some(country),
            customsOffice = officeOfTransit,
            eta = Some(eta)
          )(index)

          val result = UserAnswersReader[OfficeOfTransitDomain](
            OfficeOfTransitDomain.userAnswersReader(index).apply(Nil)
          ).run(userAnswers)

          result.value.value mustEqual expectedResult
          result.value.pages mustEqual Seq(
            OfficeOfDestinationPage,
            OfficeOfTransitCountryPage(index),
            OfficeOfTransitPage(index),
            OfficeOfTransitETAPage(index),
            OfficeOfTransitSection(index)
          )
        }

        "and office of transit is not in CL147" in {
          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, securityType1Or3)
            .setValue(OfficeOfDestinationPage, customsOffice)
            .setValue(OfficeOfDestinationInCL112Page, false)
            .setValue(OfficeOfTransitCountryPage(index), country)
            .setValue(OfficeOfTransitPage(index), officeOfTransit)
            .setValue(OfficeOfTransitInCL147Page(index), false)
            .setValue(AddOfficeOfTransitETAYesNoPage(index), false)

          val expectedResult = OfficeOfTransitDomain(
            country = Some(country),
            customsOffice = officeOfTransit,
            eta = None
          )(index)

          val result = UserAnswersReader[OfficeOfTransitDomain](
            OfficeOfTransitDomain.userAnswersReader(index).apply(Nil)
          ).run(userAnswers)

          result.value.value mustEqual expectedResult
          result.value.pages mustEqual Seq(
            OfficeOfDestinationPage,
            OfficeOfTransitCountryPage(index),
            OfficeOfTransitPage(index),
            AddOfficeOfTransitETAYesNoPage(index),
            OfficeOfTransitSection(index)
          )
        }
      }

      "when security type is not one of 'entrySummaryDeclaration' or 'entryAndExitSummaryDeclaration'" in {
        val userAnswers = emptyUserAnswers
          .setValue(SecurityDetailsTypePage, securityTypeNot1Or3)
          .setValue(OfficeOfDestinationPage, customsOffice)
          .setValue(OfficeOfDestinationInCL112Page, false)
          .setValue(OfficeOfTransitCountryPage(index), country)
          .setValue(OfficeOfTransitPage(index), officeOfTransit)
          .setValue(OfficeOfTransitInCL147Page(index), false)
          .setValue(AddOfficeOfTransitETAYesNoPage(index), false)

        val expectedResult = OfficeOfTransitDomain(
          country = Some(country),
          customsOffice = officeOfTransit,
          eta = None
        )(index)

        val result = UserAnswersReader[OfficeOfTransitDomain](
          OfficeOfTransitDomain.userAnswersReader(index).apply(Nil)
        ).run(userAnswers)

        result.value.value mustEqual expectedResult
        result.value.pages mustEqual Seq(
          OfficeOfDestinationPage,
          OfficeOfTransitCountryPage(index),
          OfficeOfTransitPage(index),
          AddOfficeOfTransitETAYesNoPage(index),
          OfficeOfTransitSection(index)
        )
      }
    }

    "cannot be parsed from user answers" - {
      "when first in sequence" in {}

      "when not first in sequence" - {
        val index = Index(1)

        "when country missing" in {
          val userAnswers = emptyUserAnswers
            .setValue(OfficeOfTransitCountryPage(Index(0)), country)

          val result = UserAnswersReader[OfficeOfTransitDomain](
            OfficeOfTransitDomain.userAnswersReader(index).apply(Nil)
          ).run(userAnswers)

          result.left.value.page mustEqual OfficeOfTransitCountryPage(index)
        }

        "when office missing" - {
          "and country defined" in {
            val userAnswers = emptyUserAnswers
              .setValue(OfficeOfTransitCountryPage(Index(0)), country)
              .setValue(OfficeOfTransitCountryPage(index), country)

            val result = UserAnswersReader[OfficeOfTransitDomain](
              OfficeOfTransitDomain.userAnswersReader(index).apply(Nil)
            ).run(userAnswers)

            result.left.value.page mustEqual OfficeOfTransitPage(index)
          }

          "and inferred country defined" in {
            val userAnswers = emptyUserAnswers
              .setValue(OfficeOfTransitCountryPage(Index(0)), country)
              .setValue(InferredOfficeOfTransitCountryPage(index), country)

            val result = UserAnswersReader[OfficeOfTransitDomain](
              OfficeOfTransitDomain.userAnswersReader(index).apply(Nil)
            ).run(userAnswers)

            result.left.value.page mustEqual OfficeOfTransitPage(index)
          }
        }

        "when security type is one of 'entrySummaryDeclaration' or 'entryAndExitSummaryDeclaration'" - {
          "and office of transit is in set CL147" - {
            "and eta missing" in {
              val userAnswers = emptyUserAnswers
                .setValue(SecurityDetailsTypePage, securityType1Or3)
                .setValue(OfficeOfTransitCountryPage(Index(0)), country)
                .setValue(OfficeOfTransitCountryPage(index), country)
                .setValue(OfficeOfTransitPage(index), officeOfTransit)
                .setValue(OfficeOfTransitInCL147Page(index), true)

              val result = UserAnswersReader[OfficeOfTransitDomain](
                OfficeOfTransitDomain.userAnswersReader(index).apply(Nil)
              ).run(userAnswers)

              result.left.value.page mustEqual OfficeOfTransitETAPage(index)
            }
          }

          "and office of transit is not in set CL147" - {
            "and eta yes/no missing" in {
              val userAnswers = emptyUserAnswers
                .setValue(SecurityDetailsTypePage, securityType1Or3)
                .setValue(OfficeOfTransitCountryPage(Index(0)), country)
                .setValue(OfficeOfTransitCountryPage(index), country)
                .setValue(OfficeOfTransitPage(index), officeOfTransit)
                .setValue(OfficeOfTransitInCL147Page(index), false)

              val result = UserAnswersReader[OfficeOfTransitDomain](
                OfficeOfTransitDomain.userAnswersReader(index).apply(Nil)
              ).run(userAnswers)

              result.left.value.page mustEqual AddOfficeOfTransitETAYesNoPage(index)
            }
          }
        }

        "when security type is not one of 'entrySummaryDeclaration' or 'entryAndExitSummaryDeclaration'" - {
          "and eta yes/no missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, securityTypeNot1Or3)
              .setValue(OfficeOfTransitCountryPage(Index(0)), country)
              .setValue(OfficeOfTransitCountryPage(index), country)
              .setValue(OfficeOfTransitPage(index), officeOfTransit)
              .setValue(OfficeOfTransitInCL147Page(index), arbitrary[Boolean].sample.value)

            val result = UserAnswersReader[OfficeOfTransitDomain](
              OfficeOfTransitDomain.userAnswersReader(index).apply(Nil)
            ).run(userAnswers)

            result.left.value.page mustEqual AddOfficeOfTransitETAYesNoPage(index)
          }
        }

        "when eta yes/no is true and eta missing" in {
          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, securityTypeNot1Or3)
            .setValue(OfficeOfTransitCountryPage(Index(0)), country)
            .setValue(OfficeOfTransitCountryPage(index), country)
            .setValue(OfficeOfTransitPage(index), officeOfTransit)
            .setValue(OfficeOfTransitInCL147Page(index), false)
            .setValue(AddOfficeOfTransitETAYesNoPage(index), true)

          val result = UserAnswersReader[OfficeOfTransitDomain](
            OfficeOfTransitDomain.userAnswersReader(index).apply(Nil)
          ).run(userAnswers)

          result.left.value.page mustEqual OfficeOfTransitETAPage(index)
        }
      }
    }
  }
}
