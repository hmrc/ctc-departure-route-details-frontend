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
import config.Constants._
import config.PhaseConfig
import generators.Generators
import models.domain.{EitherType, UserAnswersReader}
import models.reference.{Country, CustomsOffice}
import models.{DateTime, Index, Phase}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.external.{OfficeOfDepartureInCL010Page, SecurityDetailsTypePage}
import pages.routing._
import pages.transit.index._

class OfficeOfTransitDomainSpec extends SpecBase with Generators {

  "OfficeOfTransitDomain" - {

    val customsOffice = arbitrary[CustomsOffice].sample.value

    val country = arbitrary[Country].sample.value

    val eta = arbitrary[DateTime].sample.value

    val securityType1Or3 = Gen.oneOf(EntrySummaryDeclarationSecurityDetails, EntryAndExitSummaryDeclarationSecurityDetails).sample.value
    val securityType1Or2or3 =
      Gen.oneOf(EntrySummaryDeclarationSecurityDetails, ExitSummaryDeclarationSecurityDetails, EntryAndExitSummaryDeclarationSecurityDetails).sample.value
    val securityTypeNot1Or3 = Gen.oneOf(NoSecurityDetails, ExitSummaryDeclarationSecurityDetails).sample.value

    val officeOfTransit = arbitrary[CustomsOffice].sample.value

    "can be parsed from UserAnswers" - {
      "when is post transition" - {
        val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
        when(mockPhaseConfig.phase).thenReturn(Phase.PostTransition)

        "when first in sequence" - {
          val index = Index(0)

          "and office of destination is in set CL112" in {
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(OfficeOfDestinationPage, customsOffice)
              .setValue(OfficeOfDestinationInCL112Page, true)
              .setValue(OfficeOfTransitPage(index), officeOfTransit)
              .setValue(AddOfficeOfTransitETAYesNoPage(index), false)

            val expectedResult = OfficeOfTransitDomain(
              country = None,
              customsOffice = officeOfTransit,
              eta = None
            )(index)

            val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
              OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
            ).run(userAnswers)

            result.value mustBe expectedResult
          }

          "and office of destination is in 'AD'" in {
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(OfficeOfDestinationPage, customsOffice.copy(id = AD))
              .setValue(OfficeOfDestinationInCL112Page, false)
              .setValue(OfficeOfTransitPage(index), officeOfTransit)
              .setValue(AddOfficeOfTransitETAYesNoPage(index), false)

            val expectedResult = OfficeOfTransitDomain(
              country = None,
              customsOffice = officeOfTransit,
              eta = None
            )(index)

            val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
              OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
            ).run(userAnswers)

            result.value mustBe expectedResult
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

            val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
              OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
            ).run(userAnswers)

            result.value mustBe expectedResult
          }
        }

        "when not first in sequence" in {
          val index = Index(1)

          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, NoSecurityDetails)
            .setValue(OfficeOfDestinationPage, customsOffice)
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

          val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
            OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
          ).run(userAnswers)

          result.value mustBe expectedResult
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

            val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
              OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
            ).run(userAnswers)

            result.value mustBe expectedResult
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

            val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
              OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
            ).run(userAnswers)

            result.value mustBe expectedResult
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

          val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
            OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
          ).run(userAnswers)

          result.value mustBe expectedResult
        }
      }
      "when is transition" - {
        val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
        when(mockPhaseConfig.phase).thenReturn(Phase.Transition)

        val inCL112 = arbitrary[Boolean].sample.value

        "and office of destination is in 'AD'" in {
          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, NoSecurityDetails)
            .setValue(OfficeOfDestinationPage, customsOffice.copy(id = AD))
            .setValue(OfficeOfDestinationInCL112Page, inCL112)
            .setValue(OfficeOfTransitPage(index), officeOfTransit)
            .setValue(AddOfficeOfTransitETAYesNoPage(index), false)

          val expectedResult = OfficeOfTransitDomain(
            country = None,
            customsOffice = officeOfTransit,
            eta = None
          )(index)

          val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
            OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
          ).run(userAnswers)

          result.value mustBe expectedResult
        }

        "and office of destination is not in 'AD'" in {
          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, NoSecurityDetails)
            .setValue(OfficeOfDestinationPage, customsOffice)
            .setValue(OfficeOfDestinationInCL112Page, inCL112)
            .setValue(OfficeOfTransitCountryPage(index), country)
            .setValue(OfficeOfTransitPage(index), officeOfTransit)
            .setValue(AddOfficeOfTransitETAYesNoPage(index), false)

          val expectedResult = OfficeOfTransitDomain(
            country = Some(country),
            customsOffice = officeOfTransit,
            eta = None
          )(index)

          val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
            OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
          ).run(userAnswers)

          result.value mustBe expectedResult
        }

        "when security type is one of 'entrySummaryDeclaration' or 'entryAndExitSummaryDeclaration' or 'exitSummaryDeclaration'" - {
          "and office of transit is in set CL010 and office of departure is not in CL010" in {
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, securityType1Or2or3)
              .setValue(OfficeOfDestinationPage, customsOffice)
              .setValue(OfficeOfDestinationInCL112Page, false)
              .setValue(OfficeOfTransitCountryPage(index), country)
              .setValue(OfficeOfTransitPage(index), officeOfTransit)
              .setValue(OfficeOfTransitInCL010Page(index), true)
              .setValue(OfficeOfDepartureInCL010Page, false)
              .setValue(OfficeOfTransitETAPage(index), eta)

            val expectedResult = OfficeOfTransitDomain(
              country = Some(country),
              customsOffice = officeOfTransit,
              eta = Some(eta)
            )(index)

            val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
              OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
            ).run(userAnswers)

            result.value mustBe expectedResult
          }

          "and office of transit is not in CL010 or office of departure is in CL010" in {
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, securityType1Or2or3)
              .setValue(OfficeOfDestinationPage, customsOffice)
              .setValue(OfficeOfDestinationInCL112Page, false)
              .setValue(OfficeOfTransitCountryPage(index), country)
              .setValue(OfficeOfTransitPage(index), officeOfTransit)
              .setValue(OfficeOfTransitInCL010Page(index), false)
              .setValue(OfficeOfDepartureInCL010Page, true)
              .setValue(AddOfficeOfTransitETAYesNoPage(index), false)

            val expectedResult = OfficeOfTransitDomain(
              country = Some(country),
              customsOffice = officeOfTransit,
              eta = None
            )(index)

            val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
              OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
            ).run(userAnswers)

            result.value mustBe expectedResult
          }
        }

        "when security type is 'noSecurity''" in {
          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, NoSecurityDetails)
            .setValue(OfficeOfDestinationPage, customsOffice)
            .setValue(OfficeOfDestinationInCL112Page, false)
            .setValue(OfficeOfTransitCountryPage(index), country)
            .setValue(OfficeOfTransitPage(index), officeOfTransit)
            .setValue(OfficeOfTransitInCL010Page(index), true)
            .setValue(OfficeOfDepartureInCL010Page, false)
            .setValue(AddOfficeOfTransitETAYesNoPage(index), false)

          val expectedResult = OfficeOfTransitDomain(
            country = Some(country),
            customsOffice = officeOfTransit,
            eta = None
          )(index)

          val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
            OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
          ).run(userAnswers)

          result.value mustBe expectedResult
        }
      }
    }

    "cannot be parsed from user answers" - {
      "when is post transition" - {
        val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
        when(mockPhaseConfig.phase).thenReturn(Phase.PostTransition)

        "when first in sequence" in {}

        "when not first in sequence" - {
          val index = Index(1)

          "when country missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(OfficeOfTransitCountryPage(Index(0)), country)

            val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
              OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe OfficeOfTransitCountryPage(index)
          }

          "when office missing" - {
            "and country defined" in {
              val userAnswers = emptyUserAnswers
                .setValue(OfficeOfTransitCountryPage(Index(0)), country)
                .setValue(OfficeOfTransitCountryPage(index), country)

              val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
                OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
              ).run(userAnswers)

              result.left.value.page mustBe OfficeOfTransitPage(index)
            }

            "and inferred country defined" in {
              val userAnswers = emptyUserAnswers
                .setValue(OfficeOfTransitCountryPage(Index(0)), country)
                .setValue(InferredOfficeOfTransitCountryPage(index), country)

              val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
                OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
              ).run(userAnswers)

              result.left.value.page mustBe OfficeOfTransitPage(index)
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

                val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
                  OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
                ).run(userAnswers)

                result.left.value.page mustBe OfficeOfTransitETAPage(index)
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

                val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
                  OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
                ).run(userAnswers)

                result.left.value.page mustBe AddOfficeOfTransitETAYesNoPage(index)
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

              val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
                OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
              ).run(userAnswers)

              result.left.value.page mustBe AddOfficeOfTransitETAYesNoPage(index)
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

            val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
              OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe OfficeOfTransitETAPage(index)
          }
        }
      }

      "when is transition" - {
        val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
        when(mockPhaseConfig.phase).thenReturn(Phase.Transition)

        "when office of destination missing" in {
          val userAnswers = emptyUserAnswers
            .setValue(OfficeOfTransitCountryPage(Index(0)), country)

          val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
            OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
          ).run(userAnswers)

          result.left.value.page mustBe OfficeOfDestinationPage
        }

        "when office missing" - {
          "and country defined" in {
            val userAnswers = emptyUserAnswers
              .setValue(OfficeOfDestinationPage, customsOffice)
              .setValue(OfficeOfTransitCountryPage(Index(0)), country)
              .setValue(OfficeOfTransitCountryPage(index), country)

            val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
              OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe OfficeOfTransitPage(index)
          }

          "and inferred country defined" in {
            val userAnswers = emptyUserAnswers
              .setValue(OfficeOfDestinationPage, customsOffice)
              .setValue(OfficeOfTransitCountryPage(Index(0)), country)
              .setValue(InferredOfficeOfTransitCountryPage(index), country)

            val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
              OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe OfficeOfTransitPage(index)
          }
        }

        "when security type is one of 'entrySummaryDeclaration' or 'exitSummaryDeclaration' or 'entryAndExitSummaryDeclaration'" - {
          "and office of transit is in set CL010 and office of departure is not in CL010" - {
            "and eta missing" in {
              val userAnswers = emptyUserAnswers
                .setValue(SecurityDetailsTypePage, securityType1Or2or3)
                .setValue(OfficeOfDestinationPage, customsOffice)
                .setValue(OfficeOfTransitCountryPage(Index(0)), country)
                .setValue(OfficeOfTransitCountryPage(index), country)
                .setValue(OfficeOfTransitPage(index), officeOfTransit)
                .setValue(OfficeOfTransitInCL010Page(index), true)
                .setValue(OfficeOfDepartureInCL010Page, false)

              val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
                OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
              ).run(userAnswers)

              result.left.value.page mustBe OfficeOfTransitETAPage(index)
            }
          }

          "and office of transit is not in set CL010 or office of departure is in set CL010" - {
            "and eta yes/no missing" in {
              val userAnswers = emptyUserAnswers
                .setValue(SecurityDetailsTypePage, securityType1Or2or3)
                .setValue(OfficeOfDestinationPage, customsOffice)
                .setValue(OfficeOfTransitCountryPage(Index(0)), country)
                .setValue(OfficeOfTransitCountryPage(index), country)
                .setValue(OfficeOfTransitPage(index), officeOfTransit)
                .setValue(OfficeOfTransitInCL010Page(index), false)
                .setValue(OfficeOfDepartureInCL010Page, true)

              val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
                OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
              ).run(userAnswers)

              result.left.value.page mustBe AddOfficeOfTransitETAYesNoPage(index)
            }
          }
        }

        "when security type is not one of 'entrySummaryDeclaration' or 'exitSummaryDeclaration' or 'entryAndExitSummaryDeclaration'" - {
          "and eta yes/no missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(OfficeOfDestinationPage, customsOffice)
              .setValue(OfficeOfTransitCountryPage(Index(0)), country)
              .setValue(OfficeOfTransitCountryPage(index), country)
              .setValue(OfficeOfTransitPage(index), officeOfTransit)
              .setValue(OfficeOfTransitInCL010Page(index), arbitrary[Boolean].sample.value)
              .setValue(OfficeOfDepartureInCL010Page, arbitrary[Boolean].sample.value)

            val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
              OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe AddOfficeOfTransitETAYesNoPage(index)
          }
        }

        "when eta yes/no is true and eta missing" in {
          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, NoSecurityDetails)
            .setValue(OfficeOfDestinationPage, customsOffice)
            .setValue(OfficeOfTransitCountryPage(Index(0)), country)
            .setValue(OfficeOfTransitCountryPage(index), country)
            .setValue(OfficeOfTransitPage(index), officeOfTransit)
            .setValue(OfficeOfTransitInCL010Page(index), arbitrary[Boolean].sample.value)
            .setValue(OfficeOfDepartureInCL010Page, arbitrary[Boolean].sample.value)
            .setValue(AddOfficeOfTransitETAYesNoPage(index), true)

          val result: EitherType[OfficeOfTransitDomain] = UserAnswersReader[OfficeOfTransitDomain](
            OfficeOfTransitDomain.userAnswersReader(index)(mockPhaseConfig)
          ).run(userAnswers)

          result.left.value.page mustBe OfficeOfTransitETAPage(index)
        }
      }
    }
  }
}
