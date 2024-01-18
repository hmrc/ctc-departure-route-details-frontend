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

package models.journeyDomain.loadingAndUnloading

import cats.implicits._
import config.Constants.AdditionalDeclarationType._
import config.Constants.SecurityType._
import config.Constants.SpecificCircumstanceIndicator._
import config.PhaseConfig
import models.domain._
import models.journeyDomain.loadingAndUnloading.loading.LoadingDomain
import models.journeyDomain.loadingAndUnloading.unloading.UnloadingDomain
import models.journeyDomain.{JourneyDomainModel, ReaderSuccess, Stage}
import models.reference.SpecificCircumstanceIndicator
import models.{Mode, Phase, UserAnswers}
import pages.SpecificCircumstanceIndicatorPage
import pages.external.{AdditionalDeclarationTypePage, SecurityDetailsTypePage}
import pages.loadingAndUnloading.{AddPlaceOfLoadingYesNoPage, AddPlaceOfUnloadingPage}
import pages.sections.{LoadingAndUnloadingSection, Section}
import play.api.mvc.Call

case class LoadingAndUnloadingDomain(
  loading: Option[LoadingDomain],
  unloading: Option[UnloadingDomain]
) extends JourneyDomainModel {

  override def section: Option[Section[_]] =
    (loading, unloading) match {
      case (None, None) => None
      case _            => Some(LoadingAndUnloadingSection)
    }

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage): Option[Call] =
    (loading, unloading) match {
      case (None, None) => Some(controllers.routes.RouteDetailsAnswersController.onPageLoad(userAnswers.lrn))
      case _            => super.routeIfCompleted(userAnswers, mode, stage)
    }
}

object LoadingAndUnloadingDomain {

  def loadingReader(implicit phaseConfig: PhaseConfig): Read[Option[LoadingDomain]] =
    phaseConfig.phase match {
      case Phase.Transition =>
        SecurityDetailsTypePage.reader.apply(_).flatMap {
          case ReaderSuccess(NoSecurityDetails, pages) =>
            UserAnswersReader.none.apply(pages)
          case ReaderSuccess(_, pages) =>
            LoadingDomain.userAnswersReader.toOption.apply(pages)
        }
      case Phase.PostTransition =>
        AdditionalDeclarationTypePage.reader.apply(_).flatMap {
          case ReaderSuccess(PreLodge, pages) =>
            AddPlaceOfLoadingYesNoPage.filterOptionalDependent(identity)(LoadingDomain.userAnswersReader).apply(pages)
          case ReaderSuccess(_, pages) =>
            LoadingDomain.userAnswersReader.toOption.apply(pages)
        }
    }

  def unloadingReader(implicit phaseConfig: PhaseConfig): Read[Option[UnloadingDomain]] = {
    lazy val mandatoryReader: Read[Option[UnloadingDomain]] =
      UnloadingDomain.userAnswersReader.toOption

    lazy val optionalReader: Read[Option[UnloadingDomain]] =
      AddPlaceOfUnloadingPage.filterOptionalDependent(identity)(UnloadingDomain.userAnswersReader)

    phaseConfig.phase match {
      case Phase.Transition =>
        SecurityDetailsTypePage.reader.apply(_).flatMap {
          case ReaderSuccess(NoSecurityDetails, pages) =>
            UserAnswersReader.none.apply(pages)
          case ReaderSuccess(_, pages) =>
            SpecificCircumstanceIndicatorPage.optionalReader.apply(pages).flatMap {
              case ReaderSuccess(Some(SpecificCircumstanceIndicator(XXX, _)), pages) => optionalReader(pages)
              case ReaderSuccess(_, pages)                                           => mandatoryReader(pages)
            }
        }
      case Phase.PostTransition =>
        SecurityDetailsTypePage.reader.apply(_).flatMap {
          case ReaderSuccess(NoSecurityDetails, pages)                     => UserAnswersReader.none.apply(pages)
          case ReaderSuccess(ExitSummaryDeclarationSecurityDetails, pages) => optionalReader(pages)
          case ReaderSuccess(_, pages)                                     => mandatoryReader(pages)
        }
    }
  }

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): Read[LoadingAndUnloadingDomain] =
    (
      loadingReader,
      unloadingReader
    ).jdmap(LoadingAndUnloadingDomain.apply)
}
