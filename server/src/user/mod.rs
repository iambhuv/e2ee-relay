use std::error::Error;

use chrono::DateTime;
use chrono::Utc;
use scylla::DeserializeRow;
use scylla::errors::FirstRowError;

use crate::SC_POOL;

#[derive(Debug)]
pub struct User {
    key: [u8; 32],
    created_at: Option<DateTime<Utc>>,
}

#[derive(DeserializeRow)]
struct UserExistRow {
    created_at: DateTime<Utc>,
}

impl User {
    pub fn new(key: [u8; 32]) -> User {
        User { key, created_at: None }
    }

    pub fn from(pubkey: &[u8]) -> Result<User, ()> {
        let key: [u8; 32] = pubkey.try_into().map_err(|_| ())?;
        Ok(User { key, created_at: None })
    }

    /**
     * Does user exist in database?
     */
    pub async fn exists(&mut self) -> Result<bool, Box<dyn Error>> {
        let scylla = SC_POOL.get().unwrap();

        match scylla
            .query_unpaged(
                "SELECT created_at FROM known_users WHERE public_key = ?",
                (self.key.as_slice(),),
            )
            .await?
            .into_rows_result()?
            .first_row::<UserExistRow>()
        {
            Err(FirstRowError::RowsEmpty) => Ok(false),
            Err(err) => Err(Box::new(err)),
            Ok(user) => {
                self.created_at = Some(user.created_at);

                Ok(true)
            },
        }
    }
}
