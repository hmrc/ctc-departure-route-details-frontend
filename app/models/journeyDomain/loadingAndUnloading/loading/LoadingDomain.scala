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

import cats.implicits._
import models.domain.{GettableAsFilterForNextReaderOps, GettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.JourneyDomainModel
import models.reference.UnLocode
import pages.loadingAndUnloading.loading.{AddExtraInformationYesNoPage, AddUnLocodeYesNoPage, UnLocodePage}

case class LoadingDomain(
  unLocode: Option[UnLocode],
  additionalInformation: Option[AdditionalInformationDomain]
) extends JourneyDomainModel

object LoadingDomain {

  implicit val userAnswersReader: UserAnswersReader[LoadingDomain] = {

    implicit val unLocodeReads: UserAnswersReader[Option[UnLocode]] =
      AddUnLocodeYesNoPage.filterOptionalDependent(identity)(UnLocodePage.reader)

    implicit val additionalInformationReads: UserAnswersReader[Option[AdditionalInformationDomain]] =
      AddUnLocodeYesNoPage.reader.flatMap {
        case true  => AddExtraInformationYesNoPage.filterOptionalDependent(identity)(UserAnswersReader[AdditionalInformationDomain])
        case false => UserAnswersReader[AdditionalInformationDomain].map(Some(_))
      }

    (
      UserAnswersReader[Option[UnLocode]],
      UserAnswersReader[Option[AdditionalInformationDomain]]
    ).tupled.map((LoadingDomain.apply _).tupled)
  }
}
