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

package models.journeyDomain.loadingAndUnloading.unloading

import cats.implicits._
import config.Constants.SecurityType.NoSecurityDetails
import config.PhaseConfig
import models.Phase
import models.domain._
import models.journeyDomain.{JourneyDomainModel, ReaderSuccess}
import pages.external.SecurityDetailsTypePage
import pages.loadingAndUnloading.unloading.{AddExtraInformationYesNoPage, UnLocodePage, UnLocodeYesNoPage}

case class UnloadingDomain(
  unLocode: Option[String],
  additionalInformation: Option[AdditionalInformationDomain]
) extends JourneyDomainModel

object UnloadingDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): Read[UnloadingDomain] = {
    lazy val unLocodeAndAdditionalInformationReader: Read[UnloadingDomain] =
      pages =>
        UnLocodeYesNoPage.reader.apply(pages).flatMap {
          case ReaderSuccess(true, pages) =>
            (
              UnLocodePage.reader.map(_.toOption),
              optionalAdditionalInformationReader
            ).mapReads(UnloadingDomain.apply).apply(pages)
          case ReaderSuccess(false, pages) =>
            (
              UserAnswersReader.none,
              AdditionalInformationDomain.userAnswersReader.map(_.toOption)
            ).mapReads(UnloadingDomain.apply).apply(pages)
        }

    lazy val optionalAdditionalInformationReader: Read[Option[AdditionalInformationDomain]] =
      AddExtraInformationYesNoPage.filterOptionalDependent(identity)(AdditionalInformationDomain.userAnswersReader)

    phaseConfig.phase match {
      case Phase.Transition =>
        SecurityDetailsTypePage.reader.apply(_).flatMap {
          case ReaderSuccess(NoSecurityDetails, pages) =>
            (
              UserAnswersReader.none,
              optionalAdditionalInformationReader
            ).mapReads(UnloadingDomain.apply).apply(pages)
          case ReaderSuccess(_, pages) =>
            unLocodeAndAdditionalInformationReader(pages)
        }
      case Phase.PostTransition =>
        unLocodeAndAdditionalInformationReader(_)
    }
  }
}
