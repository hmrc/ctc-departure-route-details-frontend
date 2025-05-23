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

import models.journeyDomain.*
import pages.loadingAndUnloading.unloading.{AddExtraInformationYesNoPage, UnLocodePage, UnLocodeYesNoPage}

case class UnloadingDomain(
  unLocode: Option[String],
  additionalInformation: Option[AdditionalInformationDomain]
) extends JourneyDomainModel

object UnloadingDomain {

  implicit def userAnswersReader: Read[UnloadingDomain] = {
    lazy val unLocodeAndAdditionalInformationReader: Read[UnloadingDomain] =
      UnLocodeYesNoPage.reader.to {
        case true =>
          (
            UnLocodePage.reader.toOption,
            optionalAdditionalInformationReader
          ).map(UnloadingDomain.apply)
        case false =>
          (
            UserAnswersReader.none,
            AdditionalInformationDomain.userAnswersReader.toOption
          ).map(UnloadingDomain.apply)
      }

    lazy val optionalAdditionalInformationReader: Read[Option[AdditionalInformationDomain]] =
      AddExtraInformationYesNoPage.filterOptionalDependent(identity)(AdditionalInformationDomain.userAnswersReader)

    unLocodeAndAdditionalInformationReader
  }
}
