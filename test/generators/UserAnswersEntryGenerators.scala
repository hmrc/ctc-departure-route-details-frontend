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

package generators

import config.Constants.AdditionalDeclarationType._
import models.reference._
import models.{Coordinates, DateTime, DynamicAddress, ProcedureType}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.exit.AddCustomsOfficeOfExitYesNoPage
import play.api.libs.json._
import queries.Gettable

trait UserAnswersEntryGenerators {
  self: Generators =>

  def generateAnswer: PartialFunction[Gettable[?], Gen[JsValue]] = generateRouteDetailsAnswer

  private def generateRouteDetailsAnswer: PartialFunction[Gettable[?], Gen[JsValue]] =
    generateExternalAnswer orElse
      generateSpecificCircumstanceIndicatorAnswer orElse
      generateRoutingAnswer orElse
      generateTransitAnswer orElse
      generateExitAnswer orElse
      generateLocationOfGoodsAnswer orElse
      generateLoadingAndUnloadingAnswer

  private def generateExternalAnswer: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.external._
    {
      case AdditionalDeclarationTypePage => Gen.oneOf(Standard, PreLodge).map(JsString.apply)
      case ProcedureTypePage             => arbitrary[ProcedureType].map(Json.toJson(_))
      case OfficeOfDeparturePage         => arbitrary[CustomsOffice](arbitraryOfficeOfDeparture).map(Json.toJson(_))
      case OfficeOfDepartureInCL112Page  => arbitrary[Boolean].map(JsBoolean)
      case OfficeOfDepartureInCL147Page  => arbitrary[Boolean].map(JsBoolean)
      case OfficeOfDepartureInCL010Page  => arbitrary[Boolean].map(JsBoolean)
      case DeclarationTypePage           => arbitrary[String](arbitraryDeclarationType).map(Json.toJson(_))
      case SecurityDetailsTypePage       => arbitrary[String](arbitrarySecurityDetailsType).map(Json.toJson(_))
    }
  }

  private def generateSpecificCircumstanceIndicatorAnswer: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages._
    {
      case AddSpecificCircumstanceIndicatorYesNoPage => arbitrary[Boolean].map(JsBoolean)
      case SpecificCircumstanceIndicatorPage         => arbitrary[SpecificCircumstanceIndicator].map(Json.toJson(_))
    }
  }

  private def generateRoutingAnswer: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.routing._
    import pages.routing.index._
    {
      case CountryOfDestinationPage       => arbitrary[Country].map(Json.toJson(_))
      case OfficeOfDestinationPage        => arbitrary[CustomsOffice].map(Json.toJson(_))
      case OfficeOfDestinationInCL112Page => arbitrary[Boolean].map(JsBoolean)
      case BindingItineraryPage           => arbitrary[Boolean].map(JsBoolean)
      case AddCountryOfRoutingYesNoPage   => arbitrary[Boolean].map(JsBoolean)
      case CountryOfRoutingPage(_)        => arbitrary[Country].map(Json.toJson(_))
      case CountryOfRoutingInCL112Page(_) => arbitrary[Boolean].map(JsBoolean)
      case CountryOfRoutingInCL147Page(_) => arbitrary[Boolean].map(JsBoolean)
    }
  }

  private def generateTransitAnswer: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.transit._
    import pages.transit.index._
    {
      case T2DeclarationTypeYesNoPage        => arbitrary[Boolean].map(JsBoolean)
      case AddOfficeOfTransitYesNoPage       => arbitrary[Boolean].map(JsBoolean)
      case OfficeOfTransitCountryPage(_)     => arbitrary[Country].map(Json.toJson(_))
      case OfficeOfTransitPage(_)            => arbitrary[CustomsOffice].map(Json.toJson(_))
      case OfficeOfTransitInCL147Page(_)     => arbitrary[Boolean].map(JsBoolean)
      case OfficeOfTransitInCL010Page(_)     => arbitrary[Boolean].map(JsBoolean)
      case AddOfficeOfTransitETAYesNoPage(_) => arbitrary[Boolean].map(JsBoolean)
      case OfficeOfTransitETAPage(_)         => arbitrary[DateTime].map(Json.toJson(_))
    }
  }

  private def generateExitAnswer: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.exit.index._
    {
      case AddCustomsOfficeOfExitYesNoPage => arbitrary[Boolean].map(JsBoolean)
      case OfficeOfExitCountryPage(_)      => arbitrary[Country].map(Json.toJson(_))
      case OfficeOfExitPage(_)             => arbitrary[CustomsOffice].map(Json.toJson(_))
    }
  }

  private def generateLocationOfGoodsAnswer: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.locationOfGoods._
    {
      val pf: PartialFunction[Gettable[?], Gen[JsValue]] = {
        case AddLocationOfGoodsPage => arbitrary[Boolean].map(JsBoolean)
        case LocationTypePage       => arbitrary[LocationType].map(Json.toJson(_))
        case IdentificationPage     => arbitrary[LocationOfGoodsIdentification].map(Json.toJson(_))
        case AddContactYesNoPage    => arbitrary[Boolean].map(JsBoolean)
      }

      pf orElse
        generateLocationOfGoodsIdentifierAnswer orElse
        generateLocationOfGoodsContactAnswer
    }
  }

  private def generateLocationOfGoodsIdentifierAnswer: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.locationOfGoods._
    {
      case CustomsOfficeIdentifierPage => arbitrary[CustomsOffice].map(Json.toJson(_))
      case EoriPage                    => Gen.alphaNumStr.map(JsString.apply)
      case AuthorisationNumberPage     => Gen.alphaNumStr.map(JsString.apply)
      case AddIdentifierYesNoPage      => arbitrary[Boolean].map(JsBoolean)
      case AdditionalIdentifierPage    => Gen.alphaNumStr.map(JsString.apply)
      case CoordinatesPage             => arbitrary[Coordinates].map(Json.toJson(_))
      case UnLocodePage                => arbitrary[String].map(Json.toJson(_))
      case CountryPage                 => arbitrary[Country].map(Json.toJson(_))
      case AddressPage                 => arbitrary[DynamicAddress].map(Json.toJson(_))
    }
  }

  private def generateLocationOfGoodsContactAnswer: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.locationOfGoods.contact._
    {
      case NamePage            => Gen.alphaNumStr.map(JsString.apply)
      case TelephoneNumberPage => Gen.alphaNumStr.map(JsString.apply)
    }
  }

  private def generateLoadingAndUnloadingAnswer: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.loadingAndUnloading._
    {
      val pf: PartialFunction[Gettable[?], Gen[JsValue]] = {
        case AddPlaceOfUnloadingPage => arbitrary[Boolean].map(JsBoolean)
      }

      generateLoadingAnswer orElse
        pf orElse
        generateUnloadingAnswer
    }
  }

  private def generateLoadingAnswer: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.loadingAndUnloading.loading._
    {
      case AddUnLocodeYesNoPage         => arbitrary[Boolean].map(JsBoolean)
      case UnLocodePage                 => arbitrary[String].map(Json.toJson(_))
      case AddExtraInformationYesNoPage => arbitrary[Boolean].map(JsBoolean)
      case CountryPage                  => arbitrary[Country].map(Json.toJson(_))
      case LocationPage                 => Gen.alphaNumStr.map(JsString.apply)
    }
  }

  private def generateUnloadingAnswer: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.loadingAndUnloading.AddPlaceOfUnloadingPage
    import pages.loadingAndUnloading.unloading._
    {
      case AddPlaceOfUnloadingPage      => arbitrary[Boolean].map(JsBoolean)
      case UnLocodeYesNoPage            => arbitrary[Boolean].map(JsBoolean)
      case UnLocodePage                 => arbitrary[String].map(Json.toJson(_))
      case AddExtraInformationYesNoPage => arbitrary[Boolean].map(JsBoolean)
      case CountryPage                  => arbitrary[Country].map(Json.toJson(_))
      case LocationPage                 => Gen.alphaNumStr.map(JsString.apply)
    }
  }
}
