#!/opt/local/bin/ruby

require 'openssl'
require 'base64'
require 'digest/sha2'
require 'digest/md5'

class CryptUtil

  def self.encrypt(pass, value, iv)
    enc = OpenSSL::Cipher.new('aes-128-cbc')
    enc.encrypt
    enc.key = pass
    enc.iv = pass
    crypted = ""
    crypted  << enc.update(value)
    crypted << enc.final
    encrypted =  Base64.encode64(crypted)
    return encrypted.gsub(/\n/, "")
  end
  
  def self.fromHex(hex)
    int len = hex.length/2
    
  end
    
  def self.get128Digest(key)
    digest = Digest::MD5.digest(key)
    return digest
  end
  
  def self.signature(keystore_path, keystore_alias, storepass, keypass)
    command = "| keytool -exportcert -alias " + keystore_alias + " -keystore " + keystore_path + " -storepass " + storepass + " -keypass " + keypass + " | openssl sha1 -binary | openssl enc -e -a"
    result = open(command)
    signature = ""
    while !result.eof
      signature += result.gets
    end
    result.close
    return signature.chomp
  end
end

keystore_path = ARGV[0]
keystore_alias = ARGV[1]
storepass = ARGV[2]
keypass = ARGV[3]
consumer_key = ARGV[4]
consumer_secret = ARGV[5]

if consumer_secret == nil then
  print "please input all parameters\n"
  print "$ ruby devtools/greeEncrypt.rb \"path to keysore\" \"alias\" \"password of store\" \"password of key\" \"consumer key\" \"consumer secret\"\n"
  print "For example when debug.keystore is used,\n"
  print "$ ruby tools/greeEncrypt.rb ~/.android/debug.keystore androiddebugkey android android ec2s23r234235bb 8b769242443242312545fb5bcb5b2\n"
  exit(1)
end

signature = CryptUtil.signature(keystore_path, keystore_alias, storepass, keypass)
digest128 = CryptUtil.get128Digest(signature)
consumer_key = CryptUtil.encrypt(digest128, consumer_key, signature)
cosumer_secret = CryptUtil.encrypt(digest128, consumer_secret, signature)
print "\nencrypted Consumer Key    = " + consumer_key + "\n"
print "encrypted Consumer Secret = " + cosumer_secret + "\n\n"

print "\nIf you use xml to initialize GREE SDK, please copy & paste this.\n\n"
print "        <encryptedConsumerKey>"+ consumer_key +"</encryptedConsumerKey>\n"
print "        <encryptedConsumerSecret>"+ cosumer_secret +"</encryptedConsumerSecret>\n\n"
