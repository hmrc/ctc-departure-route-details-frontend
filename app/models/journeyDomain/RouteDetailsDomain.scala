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

package models.journeyDomain

import cats.implicits._
import config.Constants.AdditionalDeclarationType._
import config.Constants.DeclarationType._
import config.Constants.SecurityType._
import config.PhaseConfig
import models.Phase
import models.domain._
import models.journeyDomain.exit.ExitDomain
import models.journeyDomain.loadingAndUnloading.LoadingAndUnloadingDomain
import models.journeyDomain.locationOfGoods.LocationOfGoodsDomain
import models.journeyDomain.routing.RoutingDomain
import models.journeyDomain.transit.TransitDomain
import models.reference.SpecificCircumstanceIndicator
import pages.exit.AddCustomsOfficeOfExitYesNoPage
import pages.external.{AdditionalDeclarationTypePage, DeclarationTypePage, OfficeOfDepartureInCL147Page, SecurityDetailsTypePage}
import pages.locationOfGoods.AddLocationOfGoodsPage
import pages.sections.routing.CountriesOfRoutingSection
import pages.sections.{RouteDetailsSection, Section}
import pages.{AddSpecificCircumstanceIndicatorYesNoPage, SpecificCircumstanceIndicatorPage}

case class RouteDetailsDomain(
  specificCircumstanceIndicator: Option[SpecificCircumstanceIndicator],
  routing: RoutingDomain,
  transit: Option[TransitDomain],
  exit: Option[ExitDomain],
  locationOfGoods: Option[LocationOfGoodsDomain],
  loadingAndUnloading: LoadingAndUnloadingDomain
) extends JourneyDomainModel {

  override def page: Option[Section[_]] = Some(RouteDetailsSection)
}

object RouteDetailsDomain {

  implicit val specificCircumstanceIndicatorReader: Read[Option[SpecificCircumstanceIndicator]] =
    SecurityDetailsTypePage.reader.apply(_).flatMap {
      case ReaderSuccess(ExitSummaryDeclarationSecurityDetails | EntryAndExitSummaryDeclarationSecurityDetails, pages) =>
        AddSpecificCircumstanceIndicatorYesNoPage.filterOptionalDependent(identity)(SpecificCircumstanceIndicatorPage.reader).apply(pages)
      case ReaderSuccess(_, pages) =>
        UserAnswersReader.none.apply(pages)
    }

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[RouteDetailsDomain] =
    specificCircumstanceIndicatorReader(Nil).flatMap {
      case ReaderSuccess(specificCircumstanceIndicator, pages) =>
        RoutingDomain.userAnswersReader.apply(pages).flatMap {
          case ReaderSuccess(routing, pages) =>
            transitReader.apply(pages).flatMap {
              case ReaderSuccess(transit, pages) =>
                exitReader(transit)(pages).flatMap {
                  case ReaderSuccess(exit, pages) =>
                    locationOfGoodsReader.apply(pages).flatMap {
                      case ReaderSuccess(locationOfGoods, pages) =>
                        LoadingAndUnloadingDomain.userAnswersReader.apply(pages).map {
                          case ReaderSuccess(loadingAndUnloading, pages) =>
                            val routeDetails = RouteDetailsDomain(
                              specificCircumstanceIndicator,
                              routing,
                              transit,
                              exit,
                              locationOfGoods,
                              loadingAndUnloading
                            )
                            ReaderSuccess(routeDetails, pages.append(routeDetails.page))
                        }
                    }
                }
            }
        }
    }

  implicit def transitReader(implicit phaseConfig: PhaseConfig): Read[Option[TransitDomain]] =
    DeclarationTypePage.reader.apply(_).flatMap {
      case ReaderSuccess(TIR, pages) => UserAnswersReader.none.apply(pages)
      case ReaderSuccess(_, pages)   => TransitDomain.userAnswersReader.toOption.apply(pages)
    }

  implicit def exitReader(transit: Option[TransitDomain]): Read[Option[ExitDomain]] =
    DeclarationTypePage.reader.apply(_).flatMap {
      case ReaderSuccess(declarationType, pages) =>
        SecurityDetailsTypePage.reader.apply(pages).flatMap {
          case ReaderSuccess(securityDetails, pages) =>
            CountriesOfRoutingSection.atLeastOneCountryOfRoutingIsInCL147(pages).flatMap {
              case ReaderSuccess(atLeastOneCountryOfRoutingInCL147, pages) =>
                if (exitRequired(declarationType, securityDetails, atLeastOneCountryOfRoutingInCL147, transit)) {
                  ExitDomain.userAnswersReader.toOption.apply(pages)
                } else {
                  (atLeastOneCountryOfRoutingInCL147, transit) match {
                    case (true, Some(TransitDomain(_, list))) if list.nonEmpty =>
                      AddCustomsOfficeOfExitYesNoPage.filterOptionalDependent(identity)(ExitDomain.userAnswersReader(_)).apply(pages)
                    case _ =>
                      UserAnswersReader.none.apply(pages)
                  }
                }
            }
        }
    }

  private def exitRequired(
    declarationType: String,
    securityDetails: String,
    atLeastOneCountryOfRoutingIsInCL147: Boolean,
    transit: Option[TransitDomain]
  ): Boolean =
    (declarationType, securityDetails, atLeastOneCountryOfRoutingIsInCL147, transit) match {
      case (TIR, _, _, _)                                                        => false
      case (_, NoSecurityDetails | EntrySummaryDeclarationSecurityDetails, _, _) => false
      case (_, _, true, Some(TransitDomain(_, list))) if list.nonEmpty           => false
      case _                                                                     => true
    }

  implicit def locationOfGoodsReader(implicit phaseConfig: PhaseConfig): Read[Option[LocationOfGoodsDomain]] = {
    lazy val optionalReader: Read[Option[LocationOfGoodsDomain]] =
      AddLocationOfGoodsPage.filterOptionalDependent(identity)(LocationOfGoodsDomain.userAnswersReader)

    phaseConfig.phase match {
      case Phase.Transition =>
        optionalReader(_)
      case Phase.PostTransition =>
        AdditionalDeclarationTypePage.reader.apply(_).flatMap {
          case ReaderSuccess(PreLodge, pages) =>
            optionalReader(pages)
          case ReaderSuccess(_, pages) =>
            OfficeOfDepartureInCL147Page.reader.apply(pages).flatMap {
              case ReaderSuccess(true, pages)  => optionalReader(pages)
              case ReaderSuccess(false, pages) => LocationOfGoodsDomain.userAnswersReader.toOption.apply(pages)
            }
        }
    }
  }
}
