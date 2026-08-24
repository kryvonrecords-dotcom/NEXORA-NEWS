import admin from 'firebase-admin';
import { db } from './db';
import { AppNotification } from '../src/types';

let initialized = false;

function initializeFirebaseAdmin(): boolean {
  if (initialized) return true;

  try {
    if (admin.apps.length > 0) {
      initialized = true;
      return true;
    }

    const serviceAccountJson = process.env.FIREBASE_SERVICE_ACCOUNT_JSON;

    if (!serviceAccountJson) {
      console.warn('FIREBASE_SERVICE_ACCOUNT_JSON não configurado.');
      return false;
    }

    const serviceAccount = JSON.parse(serviceAccountJson);

    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount)
    });

    initialized = true;
    console.log('Firebase Admin inicializado com sucesso.');
    return true;
  } catch (error) {
    console.error('Erro ao inicializar Firebase Admin:', error);
    return false;
  }
}

export async function sendFcmToAll(
  notification: AppNotification
): Promise<{ total: number; sent: number; failed: number }> {

  if (!initializeFirebaseAdmin()) {
    return {
      total: 0,
      sent: 0,
      failed: 0
    };
  }

  const tokens = db.getFcmTokens();

  if (tokens.length === 0) {
    console.log('Nenhum dispositivo FCM registrado.');
    return {
      total: 0,
      sent: 0,
      failed: 0
    };
  }

  const message: admin.messaging.MulticastMessage = {
    tokens: tokens.map(item => item.token),

    notification: {
      title: notification.title,
      body: notification.body,
      imageUrl: notification.imageUrl || undefined
    },

    data: {
      id: notification.id || '',
      newsId: notification.newsId || '',
      newsSlug: notification.newsSlug || '',
      clickUrl: notification.clickUrl ||
        (notification.newsSlug
          ? `/noticia/${notification.newsSlug}`
          : '/'),
      imageUrl: notification.imageUrl || '',
      isBreaking: notification.isBreaking ? 'true' : 'false'
    },

    android: {
      priority: notification.isBreaking ? 'high' : 'normal',
      notification: {
        channelId: 'nexora_news_notifications',
        sound: 'default',
        imageUrl: notification.imageUrl || undefined
      }
    }
  };

  try {
    const response = await admin.messaging().sendEachForMulticast(message);

    let failed = 0;

    for (let i = 0; i < response.responses.length; i++) {
      const result = response.responses[i];

      if (!result.success) {
        failed++;

        const errorCode = result.error?.code || '';

        if (
          errorCode.includes('registration-token-not-registered') ||
          errorCode.includes('invalid-registration-token')
        ) {
          db.deleteFcmToken(tokens[i].token);
        }
      }
    }

    console.log(
      `FCM: ${response.successCount} enviados, ${failed} falharam.`
    );

    return {
      total: tokens.length,
      sent: response.successCount,
      failed
    };

  } catch (error) {
    console.error('Erro ao enviar FCM:', error);

    return {
      total: tokens.length,
      sent: 0,
      failed: tokens.length
    };
  }
}
