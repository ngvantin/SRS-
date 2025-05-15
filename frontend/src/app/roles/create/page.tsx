"use client"

import type React from "react"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { useRouter } from "next/navigation"
import { createRole } from "@/app/api/roles/api"
import { toast } from "@/components/ui/use-toast"
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from "@/components/ui/card";
import { Alert, AlertDescription } from "@/components/ui/alert";
import Link from "next/link"

export default function CreateRolePage() {
  const router = useRouter()
  const [roleName, setRoleName] = useState("")
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState("")

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError("")

    try {
      await createRole({ name: roleName })
      
      toast({
        title: "Success",
        description: "Role created successfully",
      })
      
      router.push("/roles")
    } catch (error) {
      console.error("Error creating role:", error)
      setError(error instanceof Error ? error.message : "Failed to create role")
      
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Failed to create role",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container mx-auto py-6 max-w-2xl">
      <Card>
        <CardHeader>
          <CardTitle>Create Role</CardTitle>
        </CardHeader>

        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-2">
              <label className="flex items-center">
                <span className="font-medium">Role Name</span>
              </label>
              <Input
                type="text"
                value={roleName}
                onChange={(e) => setRoleName(e.target.value)}
                required
              />
            </div>

            {error && (
              <Alert variant="default">
                <AlertDescription>{error}</AlertDescription>
              </Alert>
            )}

            <div className="flex justify-end gap-2">
              <Button variant="outline" type="submit" disabled={loading}>
                {loading ? "Saving..." : "Save"}
              </Button>
              <Button variant="default" asChild>
                <Link href="/roles">Back to List</Link>
              </Button>
            </div>
          </form>
        </CardContent>

        <CardFooter className="bg-gray-50 rounded-b-lg border-t">
          <div className="flex items-center text-amber-600">
            <div className="mr-2 text-2xl">💡</div>
            <p className="text-sm">
              Roles define access levels within the system. Make sure to use descriptive names for roles.
            </p>
          </div>
        </CardFooter>
      </Card>
    </div>
  );
}
