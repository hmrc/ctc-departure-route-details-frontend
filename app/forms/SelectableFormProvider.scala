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

package forms

import forms.mappings.Mappings
import models.{Selectable, SelectableList}
import play.api.data.Form

trait SelectableFormProvider extends Mappings {

  val field: String

  def apply[T <: Selectable](prefix: String, selectableList: SelectableList[T], args: Any*): Form[T] =
    Form(
      field -> selectable[T](selectableList, s"$prefix.error.required", args)
    )
}

object SelectableFormProvider {

  class OfficeFormProvider extends SelectableFormProvider {

    override val field: String = OfficeFormProvider.field
  }

  object OfficeFormProvider {
    val field: String = "office"
  }

  class CountryFormProvider extends SelectableFormProvider {

    override val field: String = CountryFormProvider.field
  }

  object CountryFormProvider {
    val field: String = "country"
  }

}
