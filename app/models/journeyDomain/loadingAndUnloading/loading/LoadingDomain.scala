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

package models.journeyDomain.loadingAndUnloading.loading

import models.journeyDomain._
import pages.loadingAndUnloading.loading.{AddExtraInformationYesNoPage, AddUnLocodeYesNoPage, UnLocodePage}

case class LoadingDomain(
  unLocode: Option[String],
  additionalInformation: Option[AdditionalInformationDomain]
) extends JourneyDomainModel

object LoadingDomain {

  implicit val userAnswersReader: Read[LoadingDomain] = {

    lazy val unLocodeReads: Read[Option[String]] =
      AddUnLocodeYesNoPage.filterOptionalDependent(identity)(UnLocodePage.reader)

    lazy val additionalInformationReads: Read[Option[AdditionalInformationDomain]] =
      AddUnLocodeYesNoPage.reader.to {
        case true =>
          AddExtraInformationYesNoPage.filterOptionalDependent(identity)(AdditionalInformationDomain.userAnswersReader)
        case false =>
          AdditionalInformationDomain.userAnswersReader.toOption
      }

    (
      unLocodeReads,
      additionalInformationReads
    ).map(LoadingDomain.apply)
  }
}
