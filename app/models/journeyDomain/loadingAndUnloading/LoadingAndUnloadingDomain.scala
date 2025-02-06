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

import cats.implicits.*
import config.Constants.AdditionalDeclarationType.*
import config.Constants.SecurityType.*
import config.Constants.SpecificCircumstanceIndicator.*
import models.journeyDomain.*
import models.journeyDomain.loadingAndUnloading.loading.LoadingDomain
import models.journeyDomain.loadingAndUnloading.unloading.UnloadingDomain
import pages.external.{AdditionalDeclarationTypePage, SecurityDetailsTypePage}
import pages.loadingAndUnloading.{AddPlaceOfLoadingYesNoPage, AddPlaceOfUnloadingPage}
import pages.sections.{LoadingAndUnloadingSection, Section}

case class LoadingAndUnloadingDomain(
  loading: Option[LoadingDomain],
  unloading: Option[UnloadingDomain]
) extends JourneyDomainModel {

  override def page: Option[Section[?]] = Some(LoadingAndUnloadingSection)

}

object LoadingAndUnloadingDomain {

  def loadingReader: Read[Option[LoadingDomain]] =
    AdditionalDeclarationTypePage.reader.to {
      case PreLodge =>
        AddPlaceOfLoadingYesNoPage.filterOptionalDependent(identity)(LoadingDomain.userAnswersReader)
      case _ =>
        LoadingDomain.userAnswersReader.toOption
    }

  def unloadingReader: Read[Option[UnloadingDomain]] = {
    lazy val mandatoryReader: Read[Option[UnloadingDomain]] =
      UnloadingDomain.userAnswersReader.apply(_).map(_.toOption)

    lazy val optionalReader: Read[Option[UnloadingDomain]] =
      AddPlaceOfUnloadingPage.filterOptionalDependent(identity)(UnloadingDomain.userAnswersReader)

    SecurityDetailsTypePage.reader.to {
      case NoSecurityDetails                     => UserAnswersReader.none
      case ExitSummaryDeclarationSecurityDetails => optionalReader
      case _                                     => mandatoryReader
    }
  }

  implicit def userAnswersReader: Read[LoadingAndUnloadingDomain] =
    (
      loadingReader,
      unloadingReader
    ).map(LoadingAndUnloadingDomain.apply)
}
