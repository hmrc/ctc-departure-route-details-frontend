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
import config.PhaseConfig
import models.Phase
import models.SecurityDetailsType.NoSecurityDetails
import models.domain.{GettableAsFilterForNextReaderOps, GettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.JourneyDomainModel
import models.reference.UnLocode
import pages.external.SecurityDetailsTypePage
import pages.loadingAndUnloading.unloading.{AddExtraInformationYesNoPage, UnLocodePage, UnLocodeYesNoPage}

case class UnloadingDomain(
  unLocode: Option[UnLocode],
  additionalInformation: Option[AdditionalInformationDomain]
) extends JourneyDomainModel

object UnloadingDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[UnloadingDomain] = {
    lazy val unLocodeAndAdditionalInformationReader = UnLocodeYesNoPage.reader.flatMap {
      case true =>
        (UnLocodePage.reader.map(Option(_)), optionalAdditionalInformationReader).tupled
      case false =>
        (none[UnLocode].pure[UserAnswersReader], UserAnswersReader[AdditionalInformationDomain].map(Option(_))).tupled
    }

    lazy val optionalAdditionalInformationReader =
      AddExtraInformationYesNoPage.filterOptionalDependent(identity)(UserAnswersReader[AdditionalInformationDomain])

    val reader = phaseConfig.phase match {
      case Phase.Transition =>
        SecurityDetailsTypePage.reader.flatMap {
          case NoSecurityDetails => (none[UnLocode].pure[UserAnswersReader], optionalAdditionalInformationReader).tupled
          case _                 => unLocodeAndAdditionalInformationReader
        }
      case Phase.PostTransition => unLocodeAndAdditionalInformationReader
    }

    reader.map((UnloadingDomain.apply _).tupled)
  }
}
