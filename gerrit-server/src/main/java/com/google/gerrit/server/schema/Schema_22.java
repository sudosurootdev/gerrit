// Copyright (C) 2009 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gerrit.server.schema;

import static com.google.gerrit.reviewdb.AccountExternalId.SCHEME_USERNAME;

import com.google.gerrit.reviewdb.Account;
import com.google.gerrit.reviewdb.AccountExternalId;
import com.google.gerrit.reviewdb.ReviewDb;
import com.google.gerrit.reviewdb.AccountExternalId.Key;
import com.google.gwtorm.client.OrmException;
import com.google.gwtorm.jdbc.JdbcSchema;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

class Schema_22 extends SchemaVersion {
  @Inject
  Schema_22(Provider<Schema_21> prior) {
    super(prior);
  }

  @Override
  protected void migrateData(ReviewDb db) throws OrmException, SQLException {
    Collection<AccountExternalId> ids = new ArrayList<AccountExternalId>();
    Statement queryStmt = ((JdbcSchema) db).getConnection().createStatement();
    try {
      ResultSet results =
          queryStmt.executeQuery(//
              "SELECT account_id, ssh_user_name"
                  + " FROM accounts" //
                  + " WHERE ssh_user_name IS NOT NULL"
                  + " AND ssh_user_name <> ''");
      while (results.next()) {
        final int accountId = results.getInt(1);
        final String userName = results.getString(2);

        final Account.Id account = new Account.Id(accountId);
        final AccountExternalId.Key key = toKey(userName);
        ids.add(new AccountExternalId(account, key));
      }
    } finally {
      queryStmt.close();
    }
    db.accountExternalIds().insert(ids);
  }

  private Key toKey(final String userName) {
    return new AccountExternalId.Key(SCHEME_USERNAME, userName);
  }
}