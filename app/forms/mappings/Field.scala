/*
 * Copyright 2025 HM Revenue & Customs
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

package forms.mappings

sealed trait Field {
  val key: String
}

object Field {

  case object MinuteField extends Field {
    override val key: String = "minute"
  }

  case object HourField extends Field {
    override val key: String = "hour"
  }

  case object DayField extends Field {
    override val key: String = "day"
  }

  case object MonthField extends Field {
    override val key: String = "month"
  }

  case object YearField extends Field {
    override val key: String = "year"
  }
}
