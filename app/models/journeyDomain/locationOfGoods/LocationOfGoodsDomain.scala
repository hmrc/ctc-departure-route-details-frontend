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

package models.journeyDomain.locationOfGoods

import cats.implicits._
import config.Constants._
import models._
import models.domain.{GettableAsFilterForNextReaderOps, GettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.reference.{Country, CustomsOffice}
import pages.locationOfGoods._
import play.api.mvc.Call

sealed trait LocationOfGoodsDomain extends JourneyDomainModel {

  val typeOfLocation: LocationType

  val additionalContact: Option[AdditionalContactDomain] = None

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage): Option[Call] =
    Some(controllers.locationOfGoods.routes.CheckYourAnswersController.onPageLoad(userAnswers.lrn, mode))
}

object LocationOfGoodsDomain {

  implicit val userAnswersReader: UserAnswersReader[LocationOfGoodsDomain] =
    LocationTypePage.reader.flatMap {
      typeOfLocation =>
        val identifierReads: UserAnswersReader[LocationOfGoodsIdentification] = InferredIdentificationPage.reader orElse IdentificationPage.reader
        identifierReads.map(_.qualifier).flatMap {
          case CustomsOfficeIdentifier       => LocationOfGoodsV.userAnswersReader(typeOfLocation)
          case EoriNumberIdentifier          => LocationOfGoodsX.userAnswersReader(typeOfLocation)
          case AuthorisationNumberIdentifier => LocationOfGoodsY.userAnswersReader(typeOfLocation)
          case UnlocodeIdentifier            => LocationOfGoodsU.userAnswersReader(typeOfLocation)
          case CoordinatesIdentifier         => LocationOfGoodsW.userAnswersReader(typeOfLocation)
          case AddressIdentifier             => LocationOfGoodsZ.userAnswersReader(typeOfLocation)
          case PostalCodeIdentifier          => LocationOfGoodsT.userAnswersReader(typeOfLocation)
          case x                             => throw new Exception(s"Unexpected Location of goods identifier value $x")
        }
    }

}

case class LocationOfGoodsV(
  typeOfLocation: LocationType,
  customsOffice: CustomsOffice
) extends LocationOfGoodsDomain {}

object LocationOfGoodsV {

  def userAnswersReader(typeOfLocation: LocationType): UserAnswersReader[LocationOfGoodsDomain] =
    (
      UserAnswersReader(typeOfLocation),
      CustomsOfficeIdentifierPage.reader
    ).tupled.map((LocationOfGoodsV.apply _).tupled)
}

case class LocationOfGoodsX(
  typeOfLocation: LocationType,
  identificationNumber: String,
  additionalIdentifier: Option[String],
  override val additionalContact: Option[AdditionalContactDomain]
) extends LocationOfGoodsDomain {}

object LocationOfGoodsX {

  def userAnswersReader(typeOfLocation: LocationType): UserAnswersReader[LocationOfGoodsDomain] =
    (
      UserAnswersReader(typeOfLocation),
      EoriPage.reader,
      AddIdentifierYesNoPage.filterOptionalDependent(identity)(AdditionalIdentifierPage.reader),
      AddContactYesNoPage.filterOptionalDependent(identity)(UserAnswersReader[AdditionalContactDomain])
    ).tupled.map((LocationOfGoodsX.apply _).tupled)
}

case class LocationOfGoodsY(
  typeOfLocation: LocationType,
  authorisationNumber: String,
  additionalIdentifier: Option[String],
  override val additionalContact: Option[AdditionalContactDomain]
) extends LocationOfGoodsDomain {}

object LocationOfGoodsY {

  def userAnswersReader(typeOfLocation: LocationType): UserAnswersReader[LocationOfGoodsDomain] =
    (
      UserAnswersReader(typeOfLocation),
      AuthorisationNumberPage.reader,
      AddIdentifierYesNoPage.filterOptionalDependent(identity)(AdditionalIdentifierPage.reader),
      AddContactYesNoPage.filterOptionalDependent(identity)(UserAnswersReader[AdditionalContactDomain])
    ).tupled.map((LocationOfGoodsY.apply _).tupled)
}

case class LocationOfGoodsW(
  typeOfLocation: LocationType,
  coordinates: Coordinates,
  override val additionalContact: Option[AdditionalContactDomain]
) extends LocationOfGoodsDomain {}

object LocationOfGoodsW {

  def userAnswersReader(typeOfLocation: LocationType): UserAnswersReader[LocationOfGoodsDomain] =
    (
      UserAnswersReader(typeOfLocation),
      CoordinatesPage.reader,
      AddContactYesNoPage.filterOptionalDependent(identity)(UserAnswersReader[AdditionalContactDomain])
    ).tupled.map((LocationOfGoodsW.apply _).tupled)
}

case class LocationOfGoodsZ(
  typeOfLocation: LocationType,
  country: Country,
  address: DynamicAddress,
  override val additionalContact: Option[AdditionalContactDomain]
) extends LocationOfGoodsDomain {}

object LocationOfGoodsZ {

  def userAnswersReader(typeOfLocation: LocationType): UserAnswersReader[LocationOfGoodsDomain] =
    (
      UserAnswersReader(typeOfLocation),
      CountryPage.reader,
      AddressPage.reader,
      AddContactYesNoPage.filterOptionalDependent(identity)(UserAnswersReader[AdditionalContactDomain])
    ).tupled.map((LocationOfGoodsZ.apply _).tupled)
}

case class LocationOfGoodsU(
  typeOfLocation: LocationType,
  unLocode: String,
  override val additionalContact: Option[AdditionalContactDomain]
) extends LocationOfGoodsDomain {}

object LocationOfGoodsU {

  def userAnswersReader(typeOfLocation: LocationType): UserAnswersReader[LocationOfGoodsDomain] =
    (
      UserAnswersReader(typeOfLocation),
      UnLocodePage.reader,
      AddContactYesNoPage.filterOptionalDependent(identity)(UserAnswersReader[AdditionalContactDomain])
    ).tupled.map((LocationOfGoodsU.apply _).tupled)
}

case class LocationOfGoodsT(
  typeOfLocation: LocationType,
  postalCodeAddress: PostalCodeAddress,
  override val additionalContact: Option[AdditionalContactDomain]
) extends LocationOfGoodsDomain {}

object LocationOfGoodsT {

  def userAnswersReader(typeOfLocation: LocationType): UserAnswersReader[LocationOfGoodsDomain] =
    (
      UserAnswersReader(typeOfLocation),
      PostalCodePage.reader,
      AddContactYesNoPage.filterOptionalDependent(identity)(UserAnswersReader[AdditionalContactDomain])
    ).tupled.map((LocationOfGoodsT.apply _).tupled)

}
