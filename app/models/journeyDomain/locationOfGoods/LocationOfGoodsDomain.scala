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
import config.Constants.LocationOfGoodsIdentifier._
import models._
import models.domain._
import models.journeyDomain.{JourneyDomainModel, ReaderSuccess, Stage}
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

  implicit val userAnswersReader: Read[LocationOfGoodsDomain] =
    UserAnswersReader.readInferred(LocationTypePage, InferredLocationTypePage).apply(_).flatMap {
      case ReaderSuccess(typeOfLocation, pages) =>
        UserAnswersReader.readInferred(IdentificationPage, InferredIdentificationPage).apply(pages).map(_.to(_.qualifier)).flatMap {
          case ReaderSuccess(CustomsOfficeIdentifier, pages)       => LocationOfGoodsV.userAnswersReader(typeOfLocation)(pages)
          case ReaderSuccess(EoriNumberIdentifier, pages)          => LocationOfGoodsX.userAnswersReader(typeOfLocation)(pages)
          case ReaderSuccess(AuthorisationNumberIdentifier, pages) => LocationOfGoodsY.userAnswersReader(typeOfLocation)(pages)
          case ReaderSuccess(UnlocodeIdentifier, pages)            => LocationOfGoodsU.userAnswersReader(typeOfLocation)(pages)
          case ReaderSuccess(CoordinatesIdentifier, pages)         => LocationOfGoodsW.userAnswersReader(typeOfLocation)(pages)
          case ReaderSuccess(AddressIdentifier, pages)             => LocationOfGoodsZ.userAnswersReader(typeOfLocation)(pages)
          case ReaderSuccess(PostalCodeIdentifier, pages)          => LocationOfGoodsT.userAnswersReader(typeOfLocation)(pages)
          case ReaderSuccess(x, _)                                 => throw new Exception(s"Unexpected Location of goods identifier value $x")
        }
    }

}

case class LocationOfGoodsV(
  typeOfLocation: LocationType,
  customsOffice: CustomsOffice
) extends LocationOfGoodsDomain

object LocationOfGoodsV {

  def userAnswersReader(typeOfLocation: LocationType): Read[LocationOfGoodsDomain] =
    (
      UserAnswersReader.success(typeOfLocation),
      CustomsOfficeIdentifierPage.reader
    ).map(LocationOfGoodsV.apply)
}

case class LocationOfGoodsX(
  typeOfLocation: LocationType,
  identificationNumber: String,
  additionalIdentifier: Option[String],
  override val additionalContact: Option[AdditionalContactDomain]
) extends LocationOfGoodsDomain

object LocationOfGoodsX {

  def userAnswersReader(typeOfLocation: LocationType): Read[LocationOfGoodsDomain] =
    (
      UserAnswersReader.success(typeOfLocation),
      EoriPage.reader,
      AddIdentifierYesNoPage.filterOptionalDependent(identity)(AdditionalIdentifierPage.reader),
      AddContactYesNoPage.filterOptionalDependent(identity)(AdditionalContactDomain.userAnswersReader)
    ).map(LocationOfGoodsX.apply)
}

case class LocationOfGoodsY(
  typeOfLocation: LocationType,
  authorisationNumber: String,
  additionalIdentifier: Option[String],
  override val additionalContact: Option[AdditionalContactDomain]
) extends LocationOfGoodsDomain

object LocationOfGoodsY {

  def userAnswersReader(typeOfLocation: LocationType): Read[LocationOfGoodsDomain] =
    (
      UserAnswersReader.success(typeOfLocation),
      AuthorisationNumberPage.reader,
      AddIdentifierYesNoPage.filterOptionalDependent(identity)(AdditionalIdentifierPage.reader),
      AddContactYesNoPage.filterOptionalDependent(identity)(AdditionalContactDomain.userAnswersReader)
    ).map(LocationOfGoodsY.apply)
}

case class LocationOfGoodsW(
  typeOfLocation: LocationType,
  coordinates: Coordinates,
  override val additionalContact: Option[AdditionalContactDomain]
) extends LocationOfGoodsDomain

object LocationOfGoodsW {

  def userAnswersReader(typeOfLocation: LocationType): Read[LocationOfGoodsDomain] =
    (
      UserAnswersReader.success(typeOfLocation),
      CoordinatesPage.reader,
      AddContactYesNoPage.filterOptionalDependent(identity)(AdditionalContactDomain.userAnswersReader)
    ).map(LocationOfGoodsW.apply)
}

case class LocationOfGoodsZ(
  typeOfLocation: LocationType,
  country: Country,
  address: DynamicAddress,
  override val additionalContact: Option[AdditionalContactDomain]
) extends LocationOfGoodsDomain

object LocationOfGoodsZ {

  def userAnswersReader(typeOfLocation: LocationType): Read[LocationOfGoodsDomain] =
    (
      UserAnswersReader.success(typeOfLocation),
      CountryPage.reader,
      AddressPage.reader,
      AddContactYesNoPage.filterOptionalDependent(identity)(AdditionalContactDomain.userAnswersReader)
    ).map(LocationOfGoodsZ.apply)
}

case class LocationOfGoodsU(
  typeOfLocation: LocationType,
  unLocode: String,
  override val additionalContact: Option[AdditionalContactDomain]
) extends LocationOfGoodsDomain

object LocationOfGoodsU {

  def userAnswersReader(typeOfLocation: LocationType): Read[LocationOfGoodsDomain] =
    (
      UserAnswersReader.success(typeOfLocation),
      UnLocodePage.reader,
      AddContactYesNoPage.filterOptionalDependent(identity)(AdditionalContactDomain.userAnswersReader)
    ).map(LocationOfGoodsU.apply)
}

case class LocationOfGoodsT(
  typeOfLocation: LocationType,
  postalCodeAddress: PostalCodeAddress,
  override val additionalContact: Option[AdditionalContactDomain]
) extends LocationOfGoodsDomain

object LocationOfGoodsT {

  def userAnswersReader(typeOfLocation: LocationType): Read[LocationOfGoodsDomain] =
    (
      UserAnswersReader.success(typeOfLocation),
      PostalCodePage.reader,
      AddContactYesNoPage.filterOptionalDependent(identity)(AdditionalContactDomain.userAnswersReader)
    ).map(LocationOfGoodsT.apply)

}
